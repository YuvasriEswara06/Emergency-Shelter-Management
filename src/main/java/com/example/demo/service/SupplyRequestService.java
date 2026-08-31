package com.example.demo.service;

import com.example.demo.entity.InventoryLocations;
import com.example.demo.entity.InventoryStock;
import com.example.demo.entity.InventoryTransactions;
import com.example.demo.entity.Items;
import com.example.demo.entity.Shelter;
import com.example.demo.entity.SupplyRequests;
import com.example.demo.repository.InventoryLocationsRepository;
import com.example.demo.repository.InventoryStockRepository;
import com.example.demo.repository.InventoryTransactionsRepository;
import com.example.demo.repository.ItemsRepository;
import com.example.demo.repository.ShelterRepository;
import com.example.demo.repository.SupplyRequestsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplyRequestService {

    private final SupplyRequestsRepository supplyRequestsRepository;
    private final ShelterRepository shelterRepository;
    private final ItemsRepository itemsRepository;
    private final InventoryLocationsRepository inventoryLocationsRepository;
    private final InventoryStockRepository inventoryStockRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    private final ActivityLogService activityLogService;

    public SupplyRequestService(SupplyRequestsRepository supplyRequestsRepository,
                                ShelterRepository shelterRepository,
                                ItemsRepository itemsRepository,
                                InventoryLocationsRepository inventoryLocationsRepository,
                                InventoryStockRepository inventoryStockRepository,
                                InventoryTransactionsRepository inventoryTransactionsRepository,
                                ActivityLogService activityLogService) {
        this.supplyRequestsRepository = supplyRequestsRepository;
        this.shelterRepository = shelterRepository;
        this.itemsRepository = itemsRepository;
        this.inventoryLocationsRepository = inventoryLocationsRepository;
        this.inventoryStockRepository = inventoryStockRepository;
        this.inventoryTransactionsRepository = inventoryTransactionsRepository;
        this.activityLogService = activityLogService;
    }

    public SupplyRequests createRequest(Integer shelterId, Integer itemId, Integer quantityRequested, String priority) {
        Shelter shelter = shelterRepository.findById(shelterId)
                .orElseThrow(() -> new IllegalArgumentException("Shelter not found: " + shelterId));
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));

        if (quantityRequested == null || quantityRequested <= 0) {
            throw new IllegalArgumentException("quantityRequested must be > 0");
        }

        SupplyRequests request = new SupplyRequests(shelter, item, quantityRequested, priority);
        request.setStatus("Pending");
        request.setRequestDate(LocalDateTime.now());
        return supplyRequestsRepository.save(request);
    }

    public SupplyRequests approveRequest(Integer requestId) {
        SupplyRequests request = supplyRequestsRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Supply request not found: " + requestId));
        if (!"Pending".equals(request.getStatus())) {
            throw new IllegalStateException("Only Pending requests can be approved");
        }
        request.setStatus("Approved");
        return supplyRequestsRepository.save(request);
    }

    public SupplyRequests rejectRequest(Integer requestId) {
        SupplyRequests request = supplyRequestsRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Supply request not found: " + requestId));
        if (!"Pending".equals(request.getStatus())) {
            throw new IllegalStateException("Only Pending requests can be rejected");
        }
        request.setStatus("Rejected");
        return supplyRequestsRepository.save(request);
    }

    /**
     * The core showcase transaction: fulfills an Approved supply request by
     * transferring stock from the Central Warehouse to the requesting
     * shelter. All 9 steps below happen inside ONE transaction — if any
     * step throws, everything already changed in this method is rolled
     * back automatically by Spring (@Transactional).
     *
     * @param requestId    the SupplyRequests row to fulfill
     * @param actingUserId the Users.user_id of whoever triggered this
     *                     (Admin/Staff) — recorded in the activity log
     */
    @Transactional
    public SupplyRequests fulfillRequest(Integer requestId, Integer actingUserId) {

        // 1. Fetch the SupplyRequests row by requestId
        SupplyRequests request = supplyRequestsRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Supply request not found: " + requestId));

        // 2. Validate request.status equals "Approved"
        if (!"Approved".equals(request.getStatus())) {
            throw new IllegalStateException("Only Approved requests can be fulfilled");
        }

        // 3. Find the Central Warehouse location (location_type = 'Central')
        InventoryLocations centralLocation = inventoryLocationsRepository.findByLocationType("Central")
                .orElseThrow(() -> new IllegalStateException("Central inventory location not found"));

        // 4. Find the InventoryStock for the item at Central
        Integer itemId = request.getItem().getItemId();
        Integer centralLocationId = centralLocation.getLocationId();
        InventoryStock centralStock = inventoryStockRepository
                .findByItemItemIdAndLocationLocationId(itemId, centralLocationId)
                .orElseThrow(() -> new IllegalStateException("Central stock for item not found"));

        // 5. Check sufficient quantity
        Integer available = centralStock.getQuantity();
        Integer needed = request.getQuantityRequested();
        if (available == null || available < needed) {
            throw new IllegalStateException(
                    "Insufficient central stock: available " + (available == null ? 0 : available)
                            + ", requested " + needed);
        }

        // 6. Decrease central stock and save
        centralStock.setQuantity(available - needed);
        inventoryStockRepository.save(centralStock);

        // 7. Find the shelter's inventory location
        Integer shelterId = request.getShelter().getShelterId();
        InventoryLocations shelterLocation = inventoryLocationsRepository.findByShelterShelterId(shelterId)
                .orElseThrow(() -> new IllegalStateException("Inventory location for shelter not found: " + shelterId));

        // 8. Find-or-create InventoryStock for the shelter and add quantity
        Integer shelterLocationId = shelterLocation.getLocationId();
        Optional<InventoryStock> shelterStockOpt =
                inventoryStockRepository.findByItemItemIdAndLocationLocationId(itemId, shelterLocationId);
        InventoryStock shelterStock;
        if (shelterStockOpt.isPresent()) {
            shelterStock = shelterStockOpt.get();
            shelterStock.setQuantity(shelterStock.getQuantity() + needed);
            inventoryStockRepository.save(shelterStock);
        } else {
            shelterStock = new InventoryStock(request.getItem(), shelterLocation, needed);
            inventoryStockRepository.save(shelterStock);
        }

        // 9. Record the transfer as an InventoryTransactions row
        InventoryTransactions tx = new InventoryTransactions(request.getItem(), centralLocation, shelterLocation, needed);
        inventoryTransactionsRepository.save(tx);

        // 10. Mark the request as Fulfilled
        request.setStatus("Fulfilled");
        request.setProcessedDate(LocalDateTime.now());
        supplyRequestsRepository.save(request);

        // 11. Log the action — if this throws for any reason, the WHOLE
        // transaction rolls back too, including the stock changes above.
        // That's intentional: an unlogged fulfillment is exactly the kind
        // of inconsistent state @Transactional exists to prevent.
        activityLogService.logAction(actingUserId, "Fulfilled supply request", "Supply_Requests", requestId);

        // 12. Return the updated request
        return request;
    }

    public List<SupplyRequests> getPendingRequests() {
        return supplyRequestsRepository.findByStatus("Pending");
    }

    public List<SupplyRequests> getRequestsByShelter(Integer shelterId) {
        return supplyRequestsRepository.findAll().stream()
                .filter(r -> r.getShelter() != null && shelterId.equals(r.getShelter().getShelterId()))
                .collect(Collectors.toList());
    }

    public List<SupplyRequests> getAllRequests() {
        return supplyRequestsRepository.findAll();
    }

    public Optional<SupplyRequests> getRequestById(Integer requestId) {
        return supplyRequestsRepository.findById(requestId);
    }
}