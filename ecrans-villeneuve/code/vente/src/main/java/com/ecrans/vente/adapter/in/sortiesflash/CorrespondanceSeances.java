package com.ecrans.vente.adapter.in.sortiesflash;
import com.ecrans.vente.domaine.commun.SeanceId;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
public class CorrespondanceSeances {
 private final Map<String,SeanceId> correspondances=new ConcurrentHashMap<>();
 public void associer(String externe, SeanceId interne) { correspondances.put(externe,interne); }
 public Optional<SeanceId> trouver(String externe) { return Optional.ofNullable(correspondances.get(externe)); }
}
