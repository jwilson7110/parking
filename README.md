
requires 

	java 17 (probably), (i think)

	docker


installation:

	make sure docker is running

	open console

	navigate to where you want to install

	execute command: git clone https://github.com/jwilson7110/parking .

	open src/main/resources/application-production.properties
	set spring.datasource.host to your network ip address (localhost and 127.0.0.1 will not work)

	execute commands:
		docker build -t parking .
		docker run -d -p 8080:8080 --name parking-container parking
		
		
		cd sql
		docker build -t parking-sql .
		docker run -d -p 5433:5432 --name parking-sql-container parking-sql


usage:

	visit /importLots to import initial data
		this takes somewhere around 30 minutes due to only being able to request one set of lat/lng at a time
		this can be executed multiple times to update the data with the file edited but there is currently no way to edit the file
		plan is to provide an interface to upload a file

	visit /importLotAvailability to import availability data
		this can be executed multiple times to provide new sets of data.
		older entries are preserved to maintain history

	visit /carparks/nearest?latitude=???&longitude=???&per_page=???&page=???
		returns json array of closest parking lots