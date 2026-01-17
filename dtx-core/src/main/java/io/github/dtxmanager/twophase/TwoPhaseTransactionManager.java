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
        if (participants == null || participants.isEmpty()) {
            throw new DtxException("二阶段参与者不能为空");
        }

        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.TWO_PHASE, attributes);
        record.updateStatus(TransactionStatus.PREPARING);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<TwoPhaseParticipant> preparedParticipants = new ArrayList<TwoPhaseParticipant>();

        for (TwoPhaseParticipant participant : participants) {
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), participant.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.prepare(context);
                branch.updateStatus(BranchStatus.PREPARED, null);
                preparedParticipants.add(participant);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.PREPARE_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.ROLLING_BACK);
                repository.update(record);
                rollbackInternal(record, preparedParticipants, branchRecords, attributes);
                throw new DtxException("二阶段 Prepare 失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        record.updateStatus(TransactionStatus.COMMITTING);
        repository.update(record);

        for (int i = 0; i < participants.size(); i++) {
            TwoPhaseParticipant participant = participants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.commit(context);
                branch.updateStatus(BranchStatus.COMMIT_SUCCESS, null);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.COMMIT_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                throw new DtxException("二阶段 Commit 失败: " + participant.getName(), ex);
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

    private void rollbackInternal(TransactionRecord record,
                                  List<TwoPhaseParticipant> preparedParticipants,
                                  List<BranchRecord> branchRecords,
                                  Map<String, String> attributes) {
        record.updateStatus(TransactionStatus.ROLLING_BACK);
        repository.update(record);

        for (int i = preparedParticipants.size() - 1; i >= 0; i--) {
            TwoPhaseParticipant participant = preparedParticipants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.TWO_PHASE,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.rollback(context);
                branch.updateStatus(BranchStatus.ROLLBACK_SUCCESS, null);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.ROLLBACK_FAILED, ex.getMessage());
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

