package si.result.eearly.service;

import si.result.eearly.domain.caretaker.Caretaker;
import si.result.eearly.domain.caretaker.UpsertCaretakerCommand;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.domain.schedule.Schedule;

import java.util.Optional;

public interface CaretakerService {

    Caretaker getCaretaker(String id) throws ValidationException;
    Caretaker upsertCaretaker(UpsertCaretakerCommand command) throws ValidationException;
}
