package io.github.dtxmanager.twophase;

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
 * 二阶段提交事务管理器。
 */
public class TwoPhaseTransactionManager {
    private final TransactionRepository repository;
    private final IdGenerator idGenerator;

    public TwoPhaseTransactionManager(TransactionRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public String execute(List<TwoPhaseParticipant> participants, Map<String, String> attributes) {
        // 没有参与者就无法执行两阶段提交
        if (participants == null || participants.isEmpty()) {
            throw new DtxException("二阶段参与者不能为空");
        }

        // 创建事务记录，并进入 Prepare 阶段
        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.TWO_PHASE, attributes);
        record.updateStatus(TransactionStatus.PREPARING);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<TwoPhaseParticipant> preparedParticipants = new ArrayList<TwoPhaseParticipant>();

        // Prepare 阶段：所有参与者先预提交，锁定资源
        for (TwoPhaseParticipant participant : participants) {
            // 为参与者创建分支记录，便于追踪执行状态
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), participant.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Prepare 成功：记录已准备完成的参与者
                participant.prepare(context);
                branch.updateStatus(BranchStatus.PREPARED, null);
                preparedParticipants.add(participant);
            } catch (Exception ex) {
                // Prepare 失败：触发回滚，并抛错返回
                branch.updateStatus(BranchStatus.PREPARE_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.ROLLING_BACK);
                repository.update(record);
                rollbackInternal(record, preparedParticipants, branchRecords, attributes);
                throw new DtxException("二阶段 Prepare 失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        // 全部 Prepare 成功后进入 Commit 阶段
        record.updateStatus(TransactionStatus.COMMITTING);
        repository.update(record);

        // Commit 阶段按原顺序提交
        for (int i = 0; i < participants.size(); i++) {
            TwoPhaseParticipant participant = participants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Commit 成功：记录分支成功
                participant.commit(context);
                branch.updateStatus(BranchStatus.COMMIT_SUCCESS, null);
            } catch (Exception ex) {
                // Commit 失败：事务整体失败（此处不做重试演示）
                branch.updateStatus(BranchStatus.COMMIT_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                throw new DtxException("二阶段 Commit 失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        // 所有 Commit 成功，事务完成
        record.updateStatus(TransactionStatus.COMPLETED);
        repository.update(record);
        return transactionId;
    }

    public TransactionRecord getRecord(String transactionId) {
        return repository.find(transactionId);
    }

    private void rollbackInternal(TransactionRecord record,
                                  List<TwoPhaseParticipant> preparedParticipants,
                                  List<BranchRecord> branchRecords,
                                  Map<String, String> attributes) {
        record.updateStatus(TransactionStatus.ROLLING_BACK);
        repository.update(record);

        // 回滚阶段：只回滚已 Prepare 成功的参与者，并按逆序执行
        for (int i = preparedParticipants.size() - 1; i >= 0; i--) {
            TwoPhaseParticipant participant = preparedParticipants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                // Rollback 成功：记录状态
                participant.rollback(context);
                branch.updateStatus(BranchStatus.ROLLBACK_SUCCESS, null);
            } catch (Exception ex) {
                // Rollback 失败：保留失败原因，等待外部补偿
                branch.updateStatus(BranchStatus.ROLLBACK_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
            }
            repository.update(record);
        }

        // 若回滚流程结束仍处于回滚中，统一标记失败
        if (record.getStatus() == TransactionStatus.ROLLING_BACK) {
            record.updateStatus(TransactionStatus.FAILED);
            repository.update(record);
        }
    }
}

