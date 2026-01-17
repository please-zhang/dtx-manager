package io.github.dtxmanager.saga;

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
 * SAGA 事务管理器，失败时执行补偿。
 */
public class SagaTransactionManager {
    private final TransactionRepository repository;
    private final IdGenerator idGenerator;

    public SagaTransactionManager(TransactionRepository repository, IdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    public String execute(List<SagaStep> steps, Map<String, String> attributes) {
        // SAGA 至少需要一个步骤，否则无法形成事务链
        if (steps == null || steps.isEmpty()) {
            throw new DtxException("SAGA 步骤不能为空");
        }

        // 创建事务记录，保存全局事务 ID
        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.SAGA, attributes);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<SagaStep> executedSteps = new ArrayList<SagaStep>();

        // Action 阶段：按顺序执行步骤
        for (SagaStep step : steps) {
            // 为每个步骤创建分支记录，便于追踪状态
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), step.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.SAGA,
                    branch.getBranchId(), step.getName(), attributes);
            try {
                // Action 成功，记录已执行步骤
                step.action(context);
                branch.updateStatus(BranchStatus.ACTION_SUCCESS, null);
                executedSteps.add(step);
            } catch (Exception ex) {
                // Action 失败：记录失败并触发补偿
                branch.updateStatus(BranchStatus.ACTION_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                compensateInternal(record, executedSteps, branchRecords, attributes);
                throw new DtxException("SAGA Action 阶段失败: " + step.getName(), ex);
            }
            repository.update(record);
        }

        // 所有 Action 成功，事务完成
        record.updateStatus(TransactionStatus.COMPLETED);
        repository.update(record);
        return transactionId;
    }

    public TransactionRecord getRecord(String transactionId) {
        return repository.find(transactionId);
    }

    private void compensateInternal(TransactionRecord record,
                                    List<SagaStep> executedSteps,
                                    List<BranchRecord> branchRecords,
                                    Map<String, String> attributes) {
        record.updateStatus(TransactionStatus.ROLLING_BACK);
        repository.update(record);

        // 补偿阶段按已执行步骤逆序回滚
        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.SAGA,
                    branch.getBranchId(), step.getName(), attributes);
            try {
                // 补偿成功，记录状态
                step.compensate(context);
                branch.updateStatus(BranchStatus.COMPENSATE_SUCCESS, null);
            } catch (Exception ex) {
                // 补偿失败：保留失败原因，等待外部介入
                branch.updateStatus(BranchStatus.COMPENSATE_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
            }
            repository.update(record);
        }

        // 若补偿流程结束仍处于回滚中，统一标记失败
        if (record.getStatus() == TransactionStatus.ROLLING_BACK) {
            record.updateStatus(TransactionStatus.FAILED);
            repository.update(record);
        }
    }
}

