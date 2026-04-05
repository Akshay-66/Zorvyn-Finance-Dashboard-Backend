package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.CreateRecordRequest;
import com.zorvyn.finance.dto.PagedResponse;
import com.zorvyn.finance.dto.RecordFilter;
import com.zorvyn.finance.dto.UpdateRecordRequest;
import com.zorvyn.finance.exception.ApiException;
import com.zorvyn.finance.model.FinancialRecord;
import com.zorvyn.finance.model.RecordType;
import com.zorvyn.finance.model.User;
import com.zorvyn.finance.repository.RecordRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class RecordService {

    private final RecordRepository recordRepository;

    public RecordService(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    public FinancialRecord createRecord(CreateRecordRequest request, User actor) {
        validateCreateRequest(request);

        FinancialRecord record = new FinancialRecord();
        record.setId(UUID.randomUUID());
        record.setAmount(request.getAmount());
        record.setType(parseType(request.getType()));
        record.setCategory(request.getCategory().trim());
        record.setDate(request.getDate());
        record.setNotes(cleanNotes(request.getNotes()));
        record.setCreatedBy(actor.getId());

        return recordRepository.create(record);
    }

    public FinancialRecord getRecord(UUID id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new ApiException(404, "Record not found"));
    }

    public PagedResponse<FinancialRecord> getRecords(RecordFilter filter) {
        validateFilter(filter);
        List<FinancialRecord> records = recordRepository.search(filter);
        long totalItems = recordRepository.count(filter);
        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / filter.getSize());

        return new PagedResponse<>(records, filter.getPage(), filter.getSize(), totalItems, totalPages);
    }

    public FinancialRecord updateRecord(UUID id, UpdateRecordRequest request) {
        validateUpdateRequest(request);
        FinancialRecord existingRecord = getRecord(id);

        existingRecord.setAmount(request.getAmount() == null ? existingRecord.getAmount() : request.getAmount());
        existingRecord.setType(request.getType() == null ? existingRecord.getType() : parseType(request.getType()));
        existingRecord.setCategory(request.getCategory() == null ? existingRecord.getCategory() : request.getCategory().trim());
        existingRecord.setDate(request.getDate() == null ? existingRecord.getDate() : request.getDate());
        existingRecord.setNotes(request.getNotes() == null ? existingRecord.getNotes() : cleanNotes(request.getNotes()));

        return recordRepository.update(existingRecord)
                .orElseThrow(() -> new ApiException(404, "Record not found"));
    }

    public void deleteRecord(UUID id) {
        if (!recordRepository.softDelete(id)) {
            throw new ApiException(404, "Record not found");
        }
    }

    private void validateCreateRequest(CreateRecordRequest request) {
        if (request == null) {
            throw new ApiException(400, "Request body is required");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(400, "amount must be greater than 0");
        }

        if (request.getType() == null || request.getType().isBlank()) {
            throw new ApiException(400, "type is required");
        }

        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new ApiException(400, "category is required");
        }

        if (request.getDate() == null) {
            throw new ApiException(400, "date is required");
        }

        parseType(request.getType());
        validateNotes(request.getNotes());
    }

    private void validateUpdateRequest(UpdateRecordRequest request) {
        if (request == null) {
            throw new ApiException(400, "Request body is required");
        }

        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(400, "amount must be greater than 0");
        }

        if (request.getType() != null && !request.getType().isBlank()) {
            parseType(request.getType());
        }

        if (request.getCategory() != null && request.getCategory().isBlank()) {
            throw new ApiException(400, "category cannot be blank");
        }

        validateNotes(request.getNotes());
    }

    private void validateFilter(RecordFilter filter) {
        if (filter.getPage() <= 0) {
            throw new ApiException(400, "page must be greater than 0");
        }

        if (filter.getSize() <= 0 || filter.getSize() > 100) {
            throw new ApiException(400, "size must be between 1 and 100");
        }

        if (filter.getFromDate() != null && filter.getToDate() != null
                && filter.getFromDate().isAfter(filter.getToDate())) {
            throw new ApiException(400, "fromDate cannot be after toDate");
        }
    }

    private RecordType parseType(String value) {
        try {
            return RecordType.from(value);
        } catch (Exception exception) {
            throw new ApiException(400, "type must be INCOME or EXPENSE");
        }
    }

    private void validateNotes(String notes) {
        if (notes != null && notes.length() > 255) {
            throw new ApiException(400, "notes can have maximum 255 characters");
        }
    }

    private String cleanNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }
}

