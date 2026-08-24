package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.caretaker.Caretaker;
import si.result.eearly.domain.caretaker.UpsertCaretakerCommand;
import si.result.eearly.domain.exception.ErrorCode;
import si.result.eearly.domain.exception.ValidationException;
import si.result.eearly.repository.CaretakerRepository;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaretakerServiceImpl implements CaretakerService {

    private final CaretakerRepository caretakerRepository;

    @Override
    public Caretaker getCaretaker(String id) throws ValidationException {
        return caretakerRepository.findById(UUID.fromString(id)).orElseThrow(() -> new ValidationException(
                ErrorCode.INTERNAL_ERROR, String.format("Caretaker with id %s does not exist", id)));
    }

    @Override
    public Caretaker upsertCaretaker(UpsertCaretakerCommand command) throws ValidationException {
        final var caretakerToUpsert = Caretaker.createProfileFromUpsertCommand(command);
        caretakerRepository.save(caretakerToUpsert);

        return caretakerToUpsert;
    }
}
