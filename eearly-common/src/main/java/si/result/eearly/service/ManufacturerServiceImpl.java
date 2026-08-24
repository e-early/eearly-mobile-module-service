package si.result.eearly.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import si.result.eearly.domain.manufacturer.Manufacturer;
import si.result.eearly.repository.ManufacturerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;

    @Override
    public List<Manufacturer> getAllManufacturers() {
        return manufacturerRepository.findAll();
    }
}
