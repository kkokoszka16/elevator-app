package com.elevator.infrastructure.adapter.web.dto;

import com.elevator.domain.model.BuildingConfig;
import com.elevator.domain.model.Direction;
import com.elevator.domain.model.Elevator;
import com.elevator.domain.model.Floor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ElevatorMapper {

    @Mapping(target = "id", expression = "java(elevator.getId().value())")
    @Mapping(target = "currentFloor", expression = "java(elevator.getCurrentFloor().number())")
    @Mapping(target = "direction", expression = "java(elevator.getDirection().name())")
    @Mapping(target = "doorState", expression = "java(elevator.getDoorState().name())")
    @Mapping(target = "state", expression = "java(elevator.getState().name())")
    @Mapping(target = "destinations", expression = "java(mapDestinations(elevator))")
    ElevatorDto toDto(Elevator elevator);

    List<ElevatorDto> toDtoList(List<Elevator> elevators);

    default List<Integer> mapDestinations(Elevator elevator) {
        return elevator.getDestinations().stream()
                .map(Floor::number)
                .sorted()
                .toList();
    }

    default ElevatorSystemStatusDto toSystemStatusDto(BuildingConfig config, List<Elevator> elevators) {
        return new ElevatorSystemStatusDto(
                config.numberOfFloors(),
                config.numberOfElevators(),
                toDtoList(elevators)
        );
    }

    default BuildingConfigDto toConfigDto(BuildingConfig config) {
        return new BuildingConfigDto(
                config.numberOfFloors(),
                config.numberOfElevators(),
                (int) config.doorOpenDuration().toSeconds(),
                (int) config.floorTravelDuration().toSeconds()
        );
    }

    default BuildingConfig toConfig(BuildingConfigDto dto) {
        return new BuildingConfig(
                dto.numberOfFloors(),
                dto.numberOfElevators(),
                Duration.ofSeconds(dto.doorOpenDurationSeconds() != null ? dto.doorOpenDurationSeconds() : 3),
                Duration.ofSeconds(dto.floorTravelDurationSeconds() != null ? dto.floorTravelDurationSeconds() : 2)
        );
    }

    default Direction toDirection(String direction) {
        return Direction.valueOf(direction.toUpperCase());
    }

    default Floor toFloor(Integer floor) {
        return new Floor(floor);
    }
}
