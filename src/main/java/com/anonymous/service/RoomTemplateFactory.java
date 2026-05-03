package com.anonymous.service;

import com.anonymous.common.exception.InvalidParameterException;
import com.anonymous.model.Seat;
import com.anonymous.model.enums.SeatStatus;

import java.util.ArrayList;
import java.util.List;

public class RoomTemplateFactory {

    private static String normalize(String code) {
        return code == null || code.isBlank() ? "CLASSROOM" : code.trim().toUpperCase();
    }

    private static List<Seat> createClassroom(Long roomId) {
        List<Seat> seats = new ArrayList<>();
        int no = 1;
        int[] groupStarts = {0, 4, 8, 12};
        int[] rows = {3, 5, 7, 9, 11};

        for (int y : rows) {
            for (int x : groupStarts) {
                seats.add(seat(roomId, no++, x, y));
                seats.add(seat(roomId, no++, x + 1, y));
            }
        }

        for (int x : new int[]{4, 5, 8, 9, 10}) {
            seats.add(seat(roomId, no++, x, 14));
        }

        return seats;
    }

    private static List<Seat> createReadingHall(Long roomId) {
        List<Seat> seats = new ArrayList<>();
        int no = 1;

        for (int row = 0; row < 20; row++) {
            int y = row * 2;
            seats.add(seat(roomId, no++, 0, y));
            seats.add(seat(roomId, no++, 1, y));
        }

        return seats;
    }

    private static List<Seat> createStudyRoomLarge(Long roomId) {
        List<Seat> seats = new ArrayList<>();
        int no = 1;

        for (int startX : new int[]{0, 10, 20}) {
            for (int col = 0; col < 8; col++) {
                seats.add(seat(roomId, no++, startX + col, 0));
            }
        }

        for (int blockY : new int[]{4, 9, 14, 19}) {
            for (int blockX : new int[]{0, 10, 20}) {
                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 6; col++) {
                        seats.add(seat(roomId, no++, blockX + col, blockY + row));
                    }
                }
            }
        }

        return seats;
    }

    public static List<Seat> createSeats(String templateCode, Long roomId) {
        return switch (normalize(templateCode)) {
            case "READING_HALL" -> createReadingHall(roomId);
            case "CLASSROOM" -> createClassroom(roomId);
            case "STUDY_ROOM_LARGE" -> createStudyRoomLarge(roomId);
            default -> throw new InvalidParameterException("room.layoutTemplate");
        };
    }

    private static Seat seat(Long roomId, int no, int x, int y) {
        Seat seat = new Seat();
        seat.setRoomId(roomId);
        seat.setSeatCode(String.valueOf(no));
        seat.setType(0);
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setXAxis(x);
        seat.setYAxis(y);
        return seat;
    }
}
