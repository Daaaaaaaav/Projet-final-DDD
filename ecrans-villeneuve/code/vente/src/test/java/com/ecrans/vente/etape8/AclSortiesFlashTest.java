package com.ecrans.vente.etape8;
import com.ecrans.vente.adapter.in.sortiesflash.*;
import com.ecrans.vente.domaine.commun.SeanceId;
import com.ecrans.vente.domaine.commande.Categorie;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class AclSortiesFlashTest {
 @Test void traduitLeFormatPartenaire() {
  var mapping=new CorrespondanceSeances(); mapping.associer("SHOW-1",new SeanceId("S42"));
  var acl=new AclSortiesFlash(mapping);
  var v=acl.traduire(new CommandeSortiesFlash("SHOW-1",java.util.List.of("A1","A2"),java.util.List.of("ADULT","CHILD"),"SF-1",1840));
  assertThat(v.seance().valeur()).isEqualTo("S42");
  assertThat(v.billets()).extracting(b->b.categorie()).containsExactly(Categorie.PLEIN,Categorie.ENFANT);
  assertThat(v.montantPaye().centimes()).isEqualTo(1840);
 }
 @Test void refuseNombreDeTypesDifferentDuNombreDePlaces() {
  var acl=new AclSortiesFlash(new CorrespondanceSeances());
  assertThatThrownBy(()->acl.traduire(new CommandeSortiesFlash("X",java.util.List.of("A1","A2"),java.util.List.of("ADULT"),"SF-2",1000))).isInstanceOf(IllegalArgumentException.class);
 }
}
