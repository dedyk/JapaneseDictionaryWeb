# -*- coding: utf-8 -*-

import sys
import datetime

import MySQLdb as mdb

if len(sys.argv) != 3:

    print "Niepoprawna liczba parametrów"

    pass

mysqlHost = "server"
mysqlUser = "user"
mysqlPass = "password"
mysqlDb = "db"

fromDate = datetime.datetime.strptime(sys.argv[1], "%Y-%m-%d")
toDate = datetime.datetime.strptime(sys.argv[2], "%Y-%m-%d").replace(hour = 23, minute = 59, second = 59)

currentDate = fromDate

try:
    conn = mdb.connect(mysqlHost, mysqlUser, mysqlPass, mysqlDb)
    
    while currentDate <= toDate:

	cur = conn.cursor()
	
	cur.execute("select count(*) from word_dictionary_search_missing_words_queue where " +
	    "first_appearance_timestamp <= %s and (lock_timestamp is null or lock_timestamp >= %s)", (str(currentDate), str(currentDate)))
	
	queueLength = cur.fetchone()
	
	print str(currentDate) + "\t" + str(queueLength[0])
	
	# currentDate = currentDate + datetime.timedelta(hours = 1)
	currentDate = currentDate + datetime.timedelta(days = 1)
	
finally:

    if conn:
	conn.close()
