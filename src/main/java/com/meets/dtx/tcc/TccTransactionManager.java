package com.meets.dtx.tcc;

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
        if (participants == null || participants.isEmpty()) {
            throw new DtxException("TCC 参与者不能为空");
        }

        String transactionId = idGenerator.nextId();
        TransactionRecord record = new TransactionRecord(transactionId, TransactionPattern.TCC, attributes);
        record.updateStatus(TransactionStatus.TRYING);
        repository.save(record);

        List<BranchRecord> branchRecords = new ArrayList<BranchRecord>();
        List<TccParticipant> triedParticipants = new ArrayList<TccParticipant>();

        for (TccParticipant participant : participants) {
            BranchRecord branch = new BranchRecord(idGenerator.nextId(), participant.getName());
            record.addBranch(branch);
            branchRecords.add(branch);
            repository.update(record);

            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.tryAction(context);
                branch.updateStatus(BranchStatus.TRY_SUCCESS, null);
                triedParticipants.add(participant);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.TRY_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                cancelInternal(record, triedParticipants, branchRecords, attributes);
                throw new DtxException("TCC Try 阶段失败: " + participant.getName(), ex);
            }
            repository.update(record);
        }

        record.updateStatus(TransactionStatus.CONFIRMING);
        repository.update(record);

        for (int i = 0; i < participants.size(); i++) {
            TccParticipant participant = participants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(transactionId, TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.confirm(context);
                branch.updateStatus(BranchStatus.CONFIRM_SUCCESS, null);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.CONFIRM_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
                repository.update(record);
                throw new DtxException("TCC Confirm 阶段失败: " + participant.getName(), ex);
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

    private void cancelInternal(TransactionRecord record,
                                List<TccParticipant> triedParticipants,
                                List<BranchRecord> branchRecords,
                                Map<String, String> attributes) {
        record.updateStatus(TransactionStatus.CANCELLING);
        repository.update(record);

        for (int i = triedParticipants.size() - 1; i >= 0; i--) {
            TccParticipant participant = triedParticipants.get(i);
            BranchRecord branch = branchRecords.get(i);
            TransactionContext context = new TransactionContext(record.getTransactionId(), TransactionPattern.TCC,
                    branch.getBranchId(), participant.getName(), attributes);
            try {
                participant.cancel(context);
                branch.updateStatus(BranchStatus.CANCEL_SUCCESS, null);
            } catch (Exception ex) {
                branch.updateStatus(BranchStatus.CANCEL_FAILED, ex.getMessage());
                record.updateStatus(TransactionStatus.FAILED);
            }
            repository.update(record);
        }

        record.updateStatus(TransactionStatus.FAILED);
        repository.update(record);
    }
}
