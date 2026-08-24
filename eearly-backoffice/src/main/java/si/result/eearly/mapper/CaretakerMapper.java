package si.result.eearly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import si.result.eearly.domain.caretaker.Caretaker;
import si.result.eearly.domain.caretaker.UpsertCaretakerCommand;
import si.result.eearly.domain.facility.Facility;
import si.result.eearly.genproto.GetCaretakerResponse;
import si.result.eearly.genproto.UpsertCaretakerRequest;
import si.result.eearly.genproto.UpsertCaretakerResponse;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface CaretakerMapper {

    default UpsertCaretakerCommand toUpsertCommand(UpsertCaretakerRequest request, String caretakerId, Facility facility) {
        var upsertCaretakerCommand = new UpsertCaretakerCommand(
                caretakerId,
                request.getCaretaker().getName(),
                facility);
        return upsertCaretakerCommand;
    }

    UpsertCaretakerResponse toUpsertCaretakerResponse(Caretaker caretaker);

    default GetCaretakerResponse toGetCaretakerResponse(Caretaker caretaker) {
        return GetCaretakerResponse.newBuilder()
                .setCaretaker(
                        si.result.eearly.genproto.Caretaker.newBuilder()
                                .setId(caretaker.getId().toString())
                                .setName(caretaker.getName())
                                .setFacilityId(caretaker.getFacility().getId().toString())
                                .build())
                .build();
    }

}
