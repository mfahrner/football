package com.theironyard.charlotte.services;

import com.theironyard.charlotte.entities.Team;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by mfahrner on 9/16/16.
 */
public interface TeamRepository extends CrudRepository<Team, Integer> {
}
