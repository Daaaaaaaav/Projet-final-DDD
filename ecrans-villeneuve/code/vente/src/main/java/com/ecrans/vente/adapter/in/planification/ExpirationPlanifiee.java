package com.ecrans.vente.adapter.in.planification;
import com.ecrans.vente.application.port.in.ExpirerBlocages;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class ExpirationPlanifiee {
 private final ExpirerBlocages expirer;
 public ExpirationPlanifiee(ExpirerBlocages expirer) { this.expirer = expirer; }
 @Scheduled(fixedRate = 60_000)
 public void chaqueMinute() { expirer.executer(); }
}
