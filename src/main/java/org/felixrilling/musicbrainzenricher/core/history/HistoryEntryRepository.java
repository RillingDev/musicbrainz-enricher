package org.felixrilling.musicbrainzenricher.core.history;

import org.felixrilling.musicbrainzenricher.core.DataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {

    @NotNull Optional<HistoryEntry> findFirstByDataTypeAndMbid(@NotNull DataType dataType, @NotNull UUID mbid);
}
