package io.github.dtxmanager.tcc;

import io.github.dtxmanager.core.BranchRecord;
import io.github.dtxmanager.core.BranchStatus;
import io.github.dtxmanager.core.DtxException;
import io.github.dtxmanager.core.IdGenerator;
import io.github.dtxmanager.core.TransactionContext;
import io.github.dtxmanager.core.TransactionPattern;
import io.github.dtxmanager.core.TransactionRecord;
import io.github.dtxmanager.core.TransactionRepository;
import io.github.dtxmanager.core.TransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TCC 事务管理器，负责 Try/Confirm/Cancel 的协调。
 */
public class TccTransactionManager {
    private final TransactionRepository repository;
    private final IdGenerator idGenerator;

    public TccTransactionManager(TransactionRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public String execute(List<TccParticipant> participants, Map<String, String> attributes) {
        // 参与者为空时无法执行事务，直接失败并抛错提示调用方
        if (participants == null || participants.isEmpty()) {
            throw new DtxException("TCC 参与者不能为空");
        }

        // 生成全局事务 ID，并创建事务记录
        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.TCC, attributes);
        record.updateStatus(TransactionStatus.TRYING);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<TccParticipant> triedParticipants = new ArrayList<TccParticipant>();

        // Try 阶段：按顺序执行每个参与者的 Try 动作
        for (TccParticipant participant : participants) {
            // 每个参与者对应一个分支记录，便于追踪执行状态
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), participant.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Try 成功：记录分支成功，并保存已完成 Try 的参与者
                participant.tryAction(context);
                branch.updateStatus(BranchStatus.TRY_SUCCESS, null);
                triedParticipants.add(participant);
            } catch (Exception ex) {
                // Try 失败：标记失败并进入 Cancel 流程
                branch.updateStatus(BranchStatus.TRY_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                cancelInternal(record, triedParticipants, branchRecords, attributes);
                throw new DtxException("TCC Try 阶段失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        // 全部 Try 成功后进入 Confirm 阶段
        record.updateStatus(TransactionStatus.CONFIRMING);
        repository.update(record);

        // Confirm 阶段必须按原顺序提交，保证业务一致性
        for (int i = 0; i < participants.size(); i++) {
            TccParticipant participant = participants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Confirm 成功：记录分支成功
                participant.confirm(context);
                branch.updateStatus(BranchStatus.CONFIRM_SUCCESS, null);
            } catch (Exception ex) {
                // Confirm 失败：事务整体失败（此处不做重试演示）
                branch.updateStatus(BranchStatus.CONFIRM_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                throw new DtxException("TCC Confirm 阶段失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        // 所有 Confirm 成功，事务完成
        record.updateStatus(TransactionStatus.COMPLETED);
        repository.update(record);
        return transactionId;
    }

    public TransactionRecord getRecord(String transactionId) {
        return repository.find(transactionId);
    }

    private void cancelInternal(TransactionRecord record,
                                List<TccParticipant> triedParticipants,
                                List<BranchRecord> branchRecords,
                                Map<String, String> attributes) {
        record.updateStatus(TransactionStatus.CANCELLING);
        repository.update(record);

        // Cancel 阶段按已 Try 成功的参与者逆序回滚
        for (int i = triedParticipants.size() - 1; i >= 0; i--) {
            TccParticipant participant = triedParticipants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Cancel 成功：记录分支成功
                participant.cancel(context);
                branch.updateStatus(BranchStatus.CANCEL_SUCCESS, null);
            } catch (Exception ex) {
                // Cancel 失败：保留失败原因，等待外部补偿或人工处理
                branch.updateStatus(BranchStatus.CANCEL_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
            }
            repository.update(record);
        }

        // 取消流程结束后，统一标记事务失败
        record.updateStatus(TransactionStatus.FAILED);
        repository.update(record);
    }
}

