package by.bsuir.wms.Service;

import by.bsuir.wms.DTO.OrganizationDTO;
import by.bsuir.wms.Entity.Organization;
import by.bsuir.wms.Entity.Employees;
import by.bsuir.wms.Repository.OrganizationRepository;
import by.bsuir.wms.Repository.EmployeesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final EmployeesRepository employeesRepository;
    private final GeocodingService geocodingService;

    public void createOrganization(OrganizationDTO organizationDTO) {

        double[] coordinates = geocodingService.getCoordinatesByAddress(organizationDTO.getAddress());

        Organization organization = Organization.builder()
                .name(organizationDTO.getName())
                .INN(organizationDTO.getInn())
                .address(organizationDTO.getAddress())
                .latitude(coordinates[0])
                .longitude(coordinates[1])
                .build();

        Employees director = findCurrentDirector();
        Optional<Organization> organizations = organizationRepository.findOrganizationByEmployees_Id(director.getId());

        if (organizations.isPresent()) {
            throw new RuntimeException("User already has an organization");
        } else {
            Organization savedOrganization = organizationRepository.save(organization);
            director.setOrganization(savedOrganization);
            employeesRepository.save(director);
        }
    }

    public void deleteOrganization() {
        Employees director = findCurrentDirector();

        Organization organization = organizationRepository.findOrganizationByEmployees_Id(director.getId())
                .orElseThrow(()->new RuntimeException("No organization for this user"));
        organizationRepository.deleteById(organization.getId());
    }

    public OrganizationDTO updateOrganization(OrganizationDTO organizationDTO) {

        Employees director = findCurrentDirector();

        Organization organization = organizationRepository.findOrganizationByEmployees_Id(director.getId())
                .orElseThrow(()->new RuntimeException("No organization for this user"));
        if (organizationDTO.getName() != null) {
            organization.setName(organizationDTO.getName());
        }
        if (organizationDTO.getInn() != null) {
            organization.setINN(organizationDTO.getInn());
        }
        if (organizationDTO.getAddress() != null) {
            double[] coordinates = geocodingService.getCoordinatesByAddress(organizationDTO.getAddress());
            organization.setAddress(organizationDTO.getAddress());
            organization.setLatitude(coordinates[0]);
            organization.setLongitude(coordinates[1]);
        }
        organizationRepository.save(organization);

        return new OrganizationDTO(organization);
    }

    public OrganizationDTO getAllOrganizations() {
        Employees director = findCurrentDirector();

        Organization organization = organizationRepository.findOrganizationByEmployees_Id(director.getId())
                .orElseThrow(()->new RuntimeException("No organization for this user"));

        return new OrganizationDTO(organization);
    }

    private Employees findCurrentDirector() {
        String currentUsername = getCurrentUsername();
        return employeesRepository.findByLogin(currentUsername)
                .filter(Employees::isDirector)
                .orElseThrow(() -> new RuntimeException("Current director not found or does not have DIRECTOR role"));
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
