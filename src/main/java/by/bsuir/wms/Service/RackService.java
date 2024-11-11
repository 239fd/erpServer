package by.bsuir.wms.Service;

import by.bsuir.wms.Repository.RackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RackService {

    private final RackRepository rackRepository;

    public void deleteRack(Integer rackId) {
        rackRepository.deleteById(rackId);
    }
}
