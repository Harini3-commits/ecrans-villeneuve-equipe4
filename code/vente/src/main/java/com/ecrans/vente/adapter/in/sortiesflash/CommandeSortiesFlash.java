package com.ecrans.vente.adapter.in.sortiesflash;

import java.util.List;

/**
 * Format JSON imposé par SortiesFlash.
 * Ces noms de champs restent dans cette ACL : ils ne sortent pas du package.
 */
public record CommandeSortiesFlash(
        String show_ref,
        List<String> seats,
        List<String> ticket_types,
        String partner_order_id,
        long paid_amount_cents
) {
}