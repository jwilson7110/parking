
requires 
	java 17 (probably), (i think)
	docker


intallation:
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


