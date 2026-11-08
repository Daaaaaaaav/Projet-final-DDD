package com.ecrans.vente.adapter.in.sortiesflash;
import com.ecrans.vente.application.port.in.EnregistrerVentePartenaire;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
public class SortiesFlashController {
 private final AclSortiesFlash acl; private final EnregistrerVentePartenaire enregistrer;
 public SortiesFlashController(AclSortiesFlash a, EnregistrerVentePartenaire e) { acl=a; enregistrer=e; }
 @PostMapping("/partner/v2/orders") @ResponseStatus(HttpStatus.CREATED)
 public SortiesFlashReponse recevoir(@RequestBody CommandeSortiesFlash c) {
  var id=enregistrer.executer(acl.traduire(c));
  return new SortiesFlashReponse(c.partner_order_id(),id.valeur(),"CONFIRMED");
 }
}
