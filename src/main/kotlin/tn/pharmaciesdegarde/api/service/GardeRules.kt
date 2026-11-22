package tn.pharmaciesdegarde.api.service

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import tn.pharmaciesdegarde.api.entity.TypeGarde

object GardeRules {
    private val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    /** Renvoie un message d'erreur, ou null si le type est cohérent avec les dates. */
    fun validate(type: TypeGarde, debut: LocalDateTime, fin: LocalDateTime): String? {
        val premierJour = debut.toLocalDate()
        // une fin à minuit pile compte comme la fin de la veille
        val dernierJour =
            if (fin.toLocalTime() == LocalTime.MIDNIGHT) fin.toLocalDate().minusDays(1)
            else fin.toLocalDate()

        return when (type) {
            TypeGarde.WEEK_END -> {
                val jours = generateSequence(premierJour) { it.plusDays(1) }
                    .takeWhile { !it.isAfter(dernierJour) }
                if (jours.all { it.dayOfWeek in WEEKEND }) null
                else "Une garde de week-end doit commencer et finir un samedi ou un dimanche"
            }
            TypeGarde.JOUR ->
                if (premierJour == dernierJour) null
                else "Une garde de jour doit se dérouler sur une seule journée"
            TypeGarde.NUIT ->
                if (debut.toLocalTime() >= LocalTime.of(18, 0) &&
                    fin.toLocalDate() == premierJour.plusDays(1) &&
                    fin.toLocalTime() <= LocalTime.NOON
                ) null
                else "Une garde de nuit doit commencer à partir de 18:00 et finir le lendemain avant 12:00"
            TypeGarde.VINGT_QUATRE_H ->
                if (Duration.between(debut, fin) == Duration.ofHours(24)) null
                else "Une garde de 24 h doit durer exactement 24 heures"
            TypeGarde.JOUR_FERIE -> null
        }
    }
}