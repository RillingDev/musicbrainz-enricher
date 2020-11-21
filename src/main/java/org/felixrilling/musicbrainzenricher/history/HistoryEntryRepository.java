package org.felixrilling.musicbrainzenricher.history;

import org.felixrilling.musicbrainzenricher.DataType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {

    @NotNull Optional<HistoryEntry> findFirstByDataTypeAndMbid(@NotNull DataType dataType, @NotNull String mbid);
}
