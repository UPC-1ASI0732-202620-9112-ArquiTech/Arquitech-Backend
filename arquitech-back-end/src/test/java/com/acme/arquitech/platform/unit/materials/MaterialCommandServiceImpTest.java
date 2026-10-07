package com.acme.arquitech.platform.unit.materials;

import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.materials.application.internal.commandservices.MaterialCommandServiceImpl;
import com.acme.arquitech.platform.materials.domain.exception.InsufficientStockException;
import com.acme.arquitech.platform.materials.domain.exception.InvalidMaterialDataException;
import com.acme.arquitech.platform.materials.domain.exception.MaterialNotFoundException;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import com.acme.arquitech.platform.materials.domain.model.aggregates.MaterialMovement;
import com.acme.arquitech.platform.materials.domain.model.commands.MaterialUsageCommand;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.MaterialMovementRepository;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.MaterialRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MaterialCommandServiceImplTest {

    private static final Long MATERIAL_ID = 1L;
    private static final Long PROJECT_ID = 10L;

    @Mock
    private MaterialRepository materialRepository;
    @Mock
    private MaterialMovementRepository movementRepository;
    @Mock
    private ProjectAccessService projectAccessService;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private MaterialCommandServiceImpl materialCommandService;

    private Material material;

    @BeforeEach
    void setUp() {
        // Material with an initial stock of 40 units
        material = new Material(PROJECT_ID, "Cemento", "bolsa", new BigDecimal("40"), new BigDecimal("5"),
                new BigDecimal("25.60"), "Constructora Lima", "20100124567", LocalDate.of(2026, 10, 1));
    }

    private MaterialUsageCommand usageOf(String quantity) {
        return new MaterialUsageCommand(new BigDecimal(quantity), OffsetDateTime.now(), "Test usage");
    }

    // The service first looks up the material's project (to check access) and then loads the material
    private void givenMaterialExists(Material existingMaterial) {
        when(materialRepository.findProjectIdById(MATERIAL_ID)).thenReturn(Optional.of(PROJECT_ID));
        when(materialRepository.findForUpdate(MATERIAL_ID)).thenReturn(Optional.of(existingMaterial));
    }

    @Test
    @DisplayName("Usage lower than stock decreases the stock")
    void usageLowerThanStockDecreasesStock() {
        givenMaterialExists(material);

        materialCommandService.use(MATERIAL_ID, usageOf("10"));

        assertEquals(new BigDecimal("30"), material.getStock());
        verify(movementRepository).save(any(MaterialMovement.class));
    }

    @Test
    @DisplayName("Usage equal to stock leaves the stock at zero")
    void usageEqualToStockLeavesStockAtZero() {
        givenMaterialExists(material);

        materialCommandService.use(MATERIAL_ID, usageOf("40"));

        assertEquals(new BigDecimal("0"), material.getStock());
    }

    @Test
    @DisplayName("Usage greater than stock throws InsufficientStockException")
    void usageGreaterThanStockThrowsException() {
        givenMaterialExists(material);

        assertThrows(InsufficientStockException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("50")));

        assertEquals(new BigDecimal("40"), material.getStock());
    }

    @Test
    @DisplayName("Usage greater than stock does not save any movement")
    void usageGreaterThanStockDoesNotSaveMovement() {
        givenMaterialExists(material);

        assertThrows(InsufficientStockException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("50")));

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usage of zero units is rejected")
    void usageOfZeroIsRejected() {
        givenMaterialExists(material);

        assertThrows(InvalidMaterialDataException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("0")));

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usage of a negative quantity is rejected")
    void negativeUsageIsRejected() {
        givenMaterialExists(material);

        assertThrows(InvalidMaterialDataException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("-5")));

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usage of a material that does not exist throws MaterialNotFoundException")
    void usageOfUnknownMaterialThrowsNotFound() {
        when(materialRepository.findProjectIdById(99L)).thenReturn(Optional.empty());

        assertThrows(MaterialNotFoundException.class,
                () -> materialCommandService.use(99L, usageOf("10")));

        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usage just above the stock (40.0001) is rejected")
    void usageJustAboveStockIsRejected() {
        givenMaterialExists(material);

        assertThrows(InsufficientStockException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("40.0001")));

        assertEquals(new BigDecimal("40"), material.getStock());
    }

    @Test
    @DisplayName("Decimal usage is subtracted correctly from a decimal stock")
    void decimalUsageIsSubtractedCorrectly() {
        Material sand = new Material(PROJECT_ID, "Arena gruesa", "m3", new BigDecimal("10.5"), new BigDecimal("1"),
                new BigDecimal("65.00"), "Agregados Lurin", "20601234561", LocalDate.of(2026, 10, 1));
        givenMaterialExists(sand);

        materialCommandService.use(MATERIAL_ID, usageOf("0.25"));

        assertEquals(new BigDecimal("10.25"), sand.getStock());
    }

    @Test
    @DisplayName("Second usage is rejected when the first one already consumed most of the stock")
    void secondUsageIsRejectedWhenStockIsNotEnough() {
        givenMaterialExists(material);

        materialCommandService.use(MATERIAL_ID, usageOf("30"));

        assertThrows(InsufficientStockException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("20")));

        assertEquals(new BigDecimal("10"), material.getStock());
    }

    @Test
    @DisplayName("Usage is denied when the user has no access to the project")
    void usageIsDeniedWithoutProjectAccess() {
        when(materialRepository.findProjectIdById(MATERIAL_ID)).thenReturn(Optional.of(PROJECT_ID));
        when(projectAccessService.requireWrite(PROJECT_ID)).thenThrow(new AccessDeniedException("Access denied"));

        assertThrows(AccessDeniedException.class,
                () -> materialCommandService.use(MATERIAL_ID, usageOf("10")));

        assertEquals(new BigDecimal("40"), material.getStock());
        verify(materialRepository, never()).findForUpdate(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Project access is not checked when the material does not exist")
    void accessIsNotCheckedWhenMaterialDoesNotExist() {
        when(materialRepository.findProjectIdById(99L)).thenReturn(Optional.empty());

        assertThrows(MaterialNotFoundException.class,
                () -> materialCommandService.use(99L, usageOf("10")));

        verify(projectAccessService, never()).requireWrite(any());
    }
}