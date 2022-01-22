package com.tenniscourts.guests;

import com.tenniscourts.config.BaseRestController;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("guests")
public class GuestController extends BaseRestController {

    private final GuestService guestService;

    @PostMapping
    public ResponseEntity<Void> addGuest(@RequestBody @Valid GuestDTO guestDTO) {
        return ResponseEntity.created(locationByEntity(guestService.addGuest(guestDTO).getId())).build();
    }

    @GetMapping
    public ResponseEntity<List<GuestDTO>> searchGuests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String name) {
        Pageable paging = PageRequest.of(page, size);
        if (!StringUtils.isEmpty(name)) {
            return ResponseEntity.ok(guestService.findGuestByName(name, paging));
        }
        return ResponseEntity.ok(guestService.findAllGuests(paging));
    }

    @GetMapping("/{guestId}")
    public ResponseEntity<GuestDTO> findGuestById(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestService.findGuestById(guestId));
    }

    @PutMapping("/{guestId}")
    public ResponseEntity<GuestDTO> updateGuest(@PathVariable Long guestId, @RequestBody @Valid GuestDTO guest) {
        return ResponseEntity.ok(guestService.updateGuest(guestId, guest));
    }

    @DeleteMapping("/{guestId}")
    public ResponseEntity<Long> deleteGuest(@PathVariable Long guestId) {
        return ResponseEntity.ok(guestService.deleteGuest(guestId));
    }

}
