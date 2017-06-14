# -*- coding: utf-8 -*-

import sys
import time

def main():
    if len(sys.argv) != 4:
        print "Niepoprawna liczba argumentów. Poprawne wywołanie to: python get_missing_words_from_log [ścieżka do pliku catalina.out] [data od] [data do]"
        print "\nData w formacie: rok-miesiac-dzien godzina-minuta-sekunda"
        
        sys.exit(1)
        
    catalina_file_name = sys.argv[1]
    
    data_from_string = sys.argv[2]
    data_to_string = sys.argv[3]
    
    date_from = None
    date_to = None
    
    #
    
    try:
        date_from = parse_date(data_from_string)
        
    except ValueError:
        print "Data w niepoprawnym formacie: " + data_from_string
        
        return
    
    #
    
    try:
        date_to = parse_date(data_to_string)
        
    except ValueError:
        print "Data w niepoprawnym formacie: " + data_to_string
        
        return
    
    #
    
    date_from_in_seconds = time.mktime(date_from)
    date_to_in_seconds = time.mktime(date_to)
    
    catalina_file = None
    
    try: 
        catalina_file = open(catalina_file_name, 'r')
            
        for line in catalina_file:
            
            line_date_string = line[0 : 19]
            line_date = None
            
            try:
                line_date = parse_date(line_date_string)
                
            except ValueError: # zla data
                continue
            
            line_date_in_seconds = time.mktime(line_date)
            
            if (line_date_in_seconds >= date_from_in_seconds and line_date_in_seconds <= date_to_in_seconds): # wpis w przedziale czasowy,
                
                find_word_request_start_template = "FindWordRequest [word="
                find_word_request_stop_template = ", searchKanji="
                
                find_word_request_start_idx = None
                find_word_request_stop_idx = None
                
                try:
                    find_word_request_start_idx = line.index(find_word_request_start_template)
                    find_word_request_stop_idx = line.index(find_word_request_stop_template, find_word_request_start_idx)
                    
                except ValueError: # ta linia nie pasuje
                    continue
                
                print line [find_word_request_start_idx + len(find_word_request_start_template) : find_word_request_stop_idx]
            
        
    except IOError, e:
        print "Nieudało się otworzyć pliku " + catalina_file_name + ". Przyczyna: " + str(e)
        
        sys.exit(1)
    
    finally:
        if catalina_file != None:
            catalina_file.close()
    

def parse_date(date_string):
    
    parsed_date = time.strptime(date_string, "%Y-%m-%d %H:%M:%S")
    
    return parsed_date

if __name__ == "__main__":
    main()
