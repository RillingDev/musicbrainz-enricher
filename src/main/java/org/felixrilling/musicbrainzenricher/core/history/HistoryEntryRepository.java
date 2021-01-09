package org.felixrilling.musicbrainzenricher.core.history;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface HistoryEntryRepository extends CrudRepository<HistoryEntry, Long> {

    @Query("SELECT he FROM HistoryEntry he WHERE he.dataType = ?1 AND he.mbid = ?2")
    @NotNull Optional<HistoryEntry> findEntryByTypeAndMbid(@NotNull DataType dataType, @NotNull UUID mbid);
}
