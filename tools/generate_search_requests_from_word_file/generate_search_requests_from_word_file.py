# -*- coding: utf-8 -*-

import sys
import urllib

def main():
    
    if len(sys.argv) != 2:
        print "Niepoprawna liczba argumentów. Poprawne wywołanie to: python generate_search_requests_from_word_file.py [nazwa pliku]"
    
        sys.exit(1)
    
    word_file_name = sys.argv[1]
    
    word_file = None
    
    try:
        word_file = open(word_file_name, 'r')
        
        counter = 0
        
        for word in word_file:
            
            word = word[0 : len(word) - 1]
            
            url_template = "https://www.japonski-pomocnik.pl/wordDictionarySearch?word=___WORD___&wordPlace=START_WITH&searchIn=KANJI&searchIn=KANA&searchIn=ROMAJI&searchIn=TRANSLATE&searchIn=INFO&_searchIn=1&dictionaryTypeStringList=WORD_VERB_IRREGULAR&dictionaryTypeStringList=WORD_VERB_AUX&dictionaryTypeStringList=WORD_VERB_ZURU&dictionaryTypeStringList=WORD_ADJECTIVE_I&dictionaryTypeStringList=WORD_AUX_ADJECTIVE_I&dictionaryTypeStringList=WORD_NAME&dictionaryTypeStringList=WORD_MALE_NAME&dictionaryTypeStringList=WORD_FEMALE_NAME&dictionaryTypeStringList=WORD_COUNTER&dictionaryTypeStringList=WORD_ADJECTIVE_NA&dictionaryTypeStringList=WORD_COMPANY_NAME&dictionaryTypeStringList=WORD_PLACE&dictionaryTypeStringList=WORD_ORGANIZATION_NAME&dictionaryTypeStringList=WORD_PRODUCT_NAME&dictionaryTypeStringList=WORD_STATION_NAME&dictionaryTypeStringList=WORD_PROPER_NOUN&dictionaryTypeStringList=WORD_SURNAME_NAME&dictionaryTypeStringList=WORD_PERSON&dictionaryTypeStringList=WORD_PARTICULE&dictionaryTypeStringList=WORD_PREFIX&dictionaryTypeStringList=WORD_SUFFIX&dictionaryTypeStringList=WORD_ADVERB&dictionaryTypeStringList=WORD_ADVERB_TO&dictionaryTypeStringList=WORD_VERB_RU&dictionaryTypeStringList=WORD_NOUN&dictionaryTypeStringList=WORD_NOUN_PREFIX&dictionaryTypeStringList=WORD_NOUN_SUFFIX&dictionaryTypeStringList=WORD_TEMPORAL_NOUN&dictionaryTypeStringList=WORD_ADVERBIAL_NOUN&dictionaryTypeStringList=WORD_ADJECTIVE_NO&dictionaryTypeStringList=WORD_ADJECTIVE_F&dictionaryTypeStringList=WORD_CONJUNCTION&dictionaryTypeStringList=WORD_AUX&dictionaryTypeStringList=WORD_ADJECTIVE_TARU&dictionaryTypeStringList=WORD_VERB_TE&dictionaryTypeStringList=WORD_VERB_U&dictionaryTypeStringList=WORD_INTERJECTION&dictionaryTypeStringList=WORD_EXPRESSION&dictionaryTypeStringList=WORD_PRONOUN&_dictionaryTypeStringList=1"
    
            word_quoted = urllib.quote_plus(word)
            
            url = url_template.replace("___WORD___", word_quoted)
            
            print word
            
            urllib.urlopen(url)            
    
    except IOError, e:
        print "Nieudało się otworzyć pliku " + word_file_name + ". Przyczyna: " + str(e)
        
        return
    
    finally:
        if word_file != None:
            word_file.close()

if __name__ == "__main__":
    main()
