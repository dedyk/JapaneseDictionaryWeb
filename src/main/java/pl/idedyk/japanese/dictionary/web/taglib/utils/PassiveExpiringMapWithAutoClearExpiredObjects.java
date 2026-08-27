package pl.idedyk.japanese.dictionary.web.taglib.utils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.map.PassiveExpiringMap;

public class PassiveExpiringMapWithAutoClearExpiredObjects<K, V> {
	
	private PassiveExpiringMap<K,V> passiveExpiringMap;
	
	private LocalDateTime nextAutoClear;
	
	public PassiveExpiringMapWithAutoClearExpiredObjects(final long timeToLive, final TimeUnit timeUnit) {
		passiveExpiringMap = new PassiveExpiringMap<K,V>(timeToLive, timeUnit);
		
		countNextAutoClearTime();
	}

	public V get(K k) {
		checkAndAutoClearExpiredObjects();

		return passiveExpiringMap.get(k);
	}
	
	public void put(K k, V v) {
		// checkAndAutoClearExpiredObjects(); // ne musi tutaj byc
		
		passiveExpiringMap.put(k, v);		
	}
	
	private void checkAndAutoClearExpiredObjects() {
		// jezeli jestesmy juz po czasie to czyscimy stare wpisy
		if (LocalDateTime.now().isAfter(nextAutoClear) == true) {
			passiveExpiringMap.size(); // to uruchomi czyszczenie wszystkich przeterminowanych obiektow
			
			countNextAutoClearTime();
		}		
	}
	
	private void countNextAutoClearTime() {
		// kolejne auto czyszczenie za 5 minut, tj. przy uzyciu
		nextAutoClear = LocalDateTime.now().plusMinutes(5);
	}
}
