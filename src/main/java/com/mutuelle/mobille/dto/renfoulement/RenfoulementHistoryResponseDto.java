package com.mutuelle.mobille.dto.renfoulement;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RenfoulementHistoryResponseDto {
    private List<RenfoulementHistoryItemDto> pastRenfoulements;
    private RenfoulementSimulationDto currentSimulation;
}