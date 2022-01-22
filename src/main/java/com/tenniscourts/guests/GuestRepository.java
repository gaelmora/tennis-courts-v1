package com.tenniscourts.guests;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GuestRepository extends PagingAndSortingRepository<Guest, Long> {

    Page<Guest> findByNameContains(String name, Pageable pageable);

    Page<Guest> findAll(Pageable pageable);
}
