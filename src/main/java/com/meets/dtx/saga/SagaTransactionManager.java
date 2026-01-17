package com.meets.dtx.saga;

import com.meets.dtx.core.BranchRecord;
import com.meets.dtx.core.BranchStatus;
import com.meets.dtx.core.DtxException;
import com.meets.dtx.core.IdGenerator;
import com.meets.dtx.core.TransactionContext;
import com.meets.dtx.core.TransactionPattern;
import com.meets.dtx.core.TransactionRecord;
import com.meets.dtx.core.TransactionRepository;
import com.meets.dtx.core.TransactionStatus;

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
        if (steps == null || steps.isEmpty()) {
            throw new DtxException("SAGA 步骤不能为空");
        }

        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.SAGA, attributes);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<SagaStep> executedSteps = new ArrayList<SagaStep>();

        for (SagaStep step : steps) {
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), step.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.SAGA,
                    branch.getBranchId(), step.getName(), attributes);
            try {
                step.action(context);
                branch.updateStatus(BranchStatus.ACTION_SUCCESS, null);
                executedSteps.add(step);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.ACTION_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                compensateInternal(record, executedSteps, branchRecords, attributes);
                throw new DtxException("SAGA Action 阶段失败: " + step.getName(), ex);
            }
            repository.update(record);
        }

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

        for (int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.SAGA,
                    branch.getBranchId(), step.getName(), attributes);
            try {
                step.compensate(context);
                branch.updateStatus(BranchStatus.COMPENSATE_SUCCESS, null);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.COMPENSATE_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
            }
            repository.update(record);
        }

        if (record.getStatus() == TransactionStatus.ROLLING_BACK) {
            record.updateStatus(TransactionStatus.FAILED);
            repository.update(record);
        }
    }
}
