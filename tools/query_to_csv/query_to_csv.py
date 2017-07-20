
import csv
import MySQLdb as mdb

def main():
    
    mysqlHost = "server"
    mysqlUser = "user"
    mysqlPass = "password"
    mysqlDb = "db"
    
    #
    
    #sql = "select * from generic_log"
    #sql = "select * from generic_log generic, word_dictionary_search_log search_log where search_log.generic_log_id = generic.id"
    #sql = "select * from word_dictionary_details_log"
    sql = "select * from word_dictionary_search_missing_words_queue"
    
    csv_output_file_name = "wynik.csv"
    
    #
    
    csv_output_file_name_handler = None
    conn = None
    
    try:        
        conn = mdb.connect(mysqlHost, mysqlUser, mysqlPass, mysqlDb, charset='utf8')
        
        cur = conn.cursor()
        cur.execute(sql)
        
        #
        
        csv_output_file_name_handler = open(csv_output_file_name, 'wb')        
        csv_writer = csv.writer(csv_output_file_name_handler, delimiter = ',', quoting=csv.QUOTE_MINIMAL, lineterminator='\n')
        
        # zapis definicji kolumn
        
        column_names = []
        
        for row_number in range(len(cur.description)):
            column_names.append(cur.description[row_number][0])
        
        csv_writer.writerow(column_names)  
        
        # zapis danych
        
        results = cur.fetchall()
        
        for row in results:
            
            column_values = []
            
            for row_number in range(len(row)):
                
                current_row_cell = row[row_number]
                
                if current_row_cell is None:
                    column_values.append(None)
                    
                elif isinstance(current_row_cell, unicode) == False:
                    column_values.append(str(row[row_number]))
                    
                else:
                    column_values.append(current_row_cell.encode("utf8"))
            
            csv_writer.writerow(column_values)
        
    finally:
        if conn:
            conn.close()
        
        if csv_output_file_name_handler:
            csv_output_file_name_handler.close()


if __name__ == "__main__":
    main()
