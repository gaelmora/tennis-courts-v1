package com.tenniscourts.guests;

import com.tenniscourts.exceptions.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GuestService {
    private final GuestRepository guestRepository;

    private final GuestMapper guestMapper;

    public GuestDTO addGuest(GuestDTO guest) {
        return guestMapper.map(guestRepository.save(guestMapper.map(guest)));
    }

    List<GuestDTO> findAllGuests(Pageable paging) {
        return guestMapper.map(guestRepository.findAll(paging).getContent());
    }

    public GuestDTO findGuestById(Long guestId) {
        return guestMapper.map(this.findById(guestId));
    }

    private Guest findById(Long guestId) {
        Optional<Guest> guest = guestRepository.findById(guestId);
        if (!guest.isPresent()) {
            throw new EntityNotFoundException("Guest not found.");
        }
        return guest.get();
    }

    public List<GuestDTO> findGuestByName(String name, Pageable paging) {
        return guestMapper.map(guestRepository.findByNameContains(name, paging).getContent());
    }

    public GuestDTO updateGuest(Long guestId, GuestDTO guestDto) {
        Guest guest = this.findById(guestId);
        guest.setName(guestDto.getName());
        return guestMapper.map(guestRepository.save(guest));
    }

    public Long deleteGuest(Long id) {
        guestRepository.delete(findById(id));
        return id;
    }


}
