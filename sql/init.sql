drop table if exists Lot;
create table Lot
(
        id uuid primary key default gen_random_uuid(),
        currentAvailabilityId uuid null,
        number varchar(6) not null,
        address varchar(100) not null,
        xCoordinate real not null,
        yCoordinate real not null,
        latitude real null,
        longitude real null,
        type varchar(50) not null,
        system varchar(50) not null,
        shortTerm varchar(50) not null,
        free varchar(50) not null,
        night varchar(50) not null,
        decks int not null,
        gantryHeight decimal not null,
        basement varchar(50) not null
);



drop table if exists LotAvailability;
create table LotAvailability
(
        id uuid primary key default gen_random_uuid(),
        lotId uuid not null,
        type varchar(2) not null,
        total int not null,
        available int not null,
        lastUpdated timestamp not null
);


