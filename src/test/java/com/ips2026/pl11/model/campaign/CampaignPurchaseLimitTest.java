package com.ips2026.pl11.model.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests of how many campaign shares a shareholder can buy in each phase
 * (US 35947), and of the purchase result.
 */
@DisplayName("CampaignPurchaseLimit")
class CampaignPurchaseLimitTest {

    /** Limit of a shareholder who owns {@code ownedOutside} shares of other campaigns and bought {@code bought} in this one. */
    private static CampaignPurchaseLimit limit(CampaignStatus status, int remaining, int ownedOutside, int bought) {
        return new CampaignPurchaseLimit(status, remaining, ownedOutside + bought, ownedOutside, bought);
    }

    @Test
    @DisplayName("phase 1: up to the shares owned of other campaigns, minus the ones already bought in it")
    void phase1() {
        assertEquals(100, limit(CampaignStatus.PHASE_1, 500, 100, 0).getMax());
        assertEquals(80, limit(CampaignStatus.PHASE_1, 500, 100, 20).getMax());
        assertEquals(0, limit(CampaignStatus.PHASE_1, 500, 100, 100).getMax());
        assertEquals(0, limit(CampaignStatus.PHASE_1, 500, 0, 0).getMax());
    }

    @Test
    @DisplayName("phase 1: never more than the remaining shares")
    void phase1LimitedByRemaining() {
        assertEquals(30, limit(CampaignStatus.PHASE_1, 30, 100, 0).getMax());
        assertEquals(0, limit(CampaignStatus.PHASE_1, 0, 100, 0).getMax());
    }

    @ParameterizedTest
    @EnumSource(value = CampaignStatus.class, names = { "PHASE_2", "PHASE_3" })
    @DisplayName("phases 2 and 3: any number of the remaining shares, owning shares or not")
    void phases2And3(CampaignStatus status) {
        assertEquals(500, limit(status, 500, 0, 0).getMax());
        assertEquals(500, limit(status, 500, 10, 300).getMax());
        assertEquals(0, limit(status, 0, 10, 0).getMax());
    }

    @ParameterizedTest
    @EnumSource(value = CampaignStatus.class, names = { "INACTIVE", "FINISHED" })
    @DisplayName("an inactive or finished campaign does not sell shares")
    void closedPhases(CampaignStatus status) {
        CampaignPurchaseLimit closed = limit(status, 500, 100, 0);
        assertEquals(0, closed.getMax());
        assertTrue(closed.getReasonForNone().contains("not open"));
        assertFalse(status.allowsShareholderPurchases());
    }

    @Test
    @DisplayName("the reason explains why nothing can be bought")
    void reasons() {
        assertTrue(limit(CampaignStatus.PHASE_2, 0, 100, 0).getReasonForNone().contains("have been sold"));
        assertTrue(limit(CampaignStatus.PHASE_1, 500, 100, 100).getReasonForNone().contains("already bought 100"));
        assertTrue(limit(CampaignStatus.PHASE_1, 500, 0, 0).getReasonForNone().contains("you have none"));
    }

    @Test
    @DisplayName("the total of a purchase is the price per share times the shares")
    void purchaseTotal() {
        CampaignPurchase purchase = new CampaignPurchase("Laura Pérez Gil", "Capital increase 2027", 20, 3101, 3120,
                new BigDecimal("12.00"));
        assertEquals(new BigDecimal("240.00"), purchase.getTotal());
    }
}
