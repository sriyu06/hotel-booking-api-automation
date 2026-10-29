package com.hotelbooking.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.hotelbooking.base.BaseTest;
import com.hotelbooking.models.Booking;
import com.hotelbooking.utils.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** One complete life-cycle of a booking. The test creates and removes its own data. */
class BookingWorkflowTests extends BaseTest {

    @Test
    @DisplayName("Create -> read -> update -> read -> delete -> confirm gone")
    void fullBookingLifecycle() {
        // 1. create
        Booking original = TestDataFactory.validBooking();
        int id = createBooking(original);

        // 2. read and verify
        Booking afterCreate = bookings.get(id).then().statusCode(200).extract().as(Booking.class);
        assertEquals(original, afterCreate, "step 2: stored data must equal what we sent");

        // 3. update
        Booking changed = TestDataFactory.updatedBooking();
        bookings.put(id, changed, token()).then().statusCode(200);

        // 4. read and verify the changes
        Booking afterUpdate = bookings.get(id).then().statusCode(200).extract().as(Booking.class);
        assertEquals(changed, afterUpdate, "step 4: stored data must equal the update");

        // 5. delete
        bookings.delete(id, token()).then().statusCode(201);

        // 6. confirm deletion
        bookings.get(id).then().statusCode(404);
    }
}
