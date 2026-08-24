package si.result.eearly.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.result.eearly.genproto.ManufacturerProto.GetManufacturersResponse;
import si.result.eearly.mapper.ManufacturerMapper;
import si.result.eearly.service.ManufacturerService;


@Service
@RequiredArgsConstructor
public class ManufacturerServiceGrpcFacade {

    private final ManufacturerService manufacturerService;
    private final ManufacturerMapper manufacturerMapper;

    @Transactional(readOnly = true)
    public GetManufacturersResponse getAllManufacturers() {
       return manufacturerMapper.toGetManufacturersResponse(manufacturerService.getAllManufacturers());
    }
}
