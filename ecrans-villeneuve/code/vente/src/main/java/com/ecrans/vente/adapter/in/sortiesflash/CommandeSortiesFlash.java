package com.ecrans.vente.adapter.in.sortiesflash;
import java.util.List;
/** Format externe conservé exclusivement dans l'ACL SortiesFlash. */
public record CommandeSortiesFlash(String show_ref, List<String> seats, List<String> ticket_types,
                                   String partner_order_id, long paid_amount_cents) {}
