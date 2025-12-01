-- Migration script to convert all ID columns from BIGINT to UUID
-- This script should be run manually before starting the application

-- 1. Drop all foreign key constraints first
ALTER TABLE IF EXISTS agencies DROP CONSTRAINT IF EXISTS fk_agencies_district;
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS fk_users_agency;
ALTER TABLE IF EXISTS ticket_orders DROP CONSTRAINT IF EXISTS fk_orders_buyer;
ALTER TABLE IF EXISTS tickets DROP CONSTRAINT IF EXISTS fk_tickets_trip;
ALTER TABLE IF EXISTS tickets DROP CONSTRAINT IF EXISTS fk_tickets_order;
ALTER TABLE IF EXISTS trips DROP CONSTRAINT IF EXISTS fk_trips_voyage;
ALTER TABLE IF EXISTS trips DROP CONSTRAINT IF EXISTS fk_trips_bus;
ALTER TABLE IF EXISTS trips DROP CONSTRAINT IF EXISTS fk_trips_driver;
ALTER TABLE IF EXISTS trips DROP CONSTRAINT IF EXISTS fk_trips_host;
ALTER TABLE IF EXISTS voyages DROP CONSTRAINT IF EXISTS fk_voyages_route;
ALTER TABLE IF EXISTS routes DROP CONSTRAINT IF EXISTS fk_routes_departure;
ALTER TABLE IF EXISTS routes DROP CONSTRAINT IF EXISTS fk_routes_arrival;
ALTER TABLE IF EXISTS route_stops DROP CONSTRAINT IF EXISTS fk_stops_route;
ALTER TABLE IF EXISTS route_stops DROP CONSTRAINT IF EXISTS fk_stops_district;
ALTER TABLE IF EXISTS districts DROP CONSTRAINT IF EXISTS fk_districts_city;

-- 2. Add new UUID columns
ALTER TABLE cities ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE districts ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE agencies ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE agencies ADD COLUMN new_district_id UUID;
ALTER TABLE users ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE users ADD COLUMN new_agency_id UUID;
ALTER TABLE buses ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE routes ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE routes ADD COLUMN new_departure_district_id UUID;
ALTER TABLE routes ADD COLUMN new_arrival_district_id UUID;
ALTER TABLE route_stops ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE route_stops ADD COLUMN new_route_id UUID;
ALTER TABLE route_stops ADD COLUMN new_district_id UUID;
ALTER TABLE voyages ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE voyages ADD COLUMN new_route_id UUID;
ALTER TABLE trips ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE trips ADD COLUMN new_voyage_id UUID;
ALTER TABLE trips ADD COLUMN new_bus_id UUID;
ALTER TABLE trips ADD COLUMN new_driver_user_id UUID;
ALTER TABLE trips ADD COLUMN new_host_user_id UUID;
ALTER TABLE ticket_orders ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE ticket_orders ADD COLUMN new_buyer_id UUID;
ALTER TABLE tickets ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE tickets ADD COLUMN new_trip_id UUID;
ALTER TABLE tickets ADD COLUMN new_order_id UUID;
ALTER TABLE expenses ADD COLUMN new_id UUID DEFAULT gen_random_uuid();

-- 3. Create mapping tables to preserve relationships
CREATE TEMP TABLE city_id_map AS SELECT id AS old_id, new_id FROM cities;
CREATE TEMP TABLE district_id_map AS SELECT id AS old_id, new_id FROM districts;
CREATE TEMP TABLE agency_id_map AS SELECT id AS old_id, new_id FROM agencies;
CREATE TEMP TABLE user_id_map AS SELECT id AS old_id, new_id FROM users;
CREATE TEMP TABLE bus_id_map AS SELECT id AS old_id, new_id FROM buses;
CREATE TEMP TABLE route_id_map AS SELECT id AS old_id, new_id FROM routes;
CREATE TEMP TABLE route_stop_id_map AS SELECT id AS old_id, new_id FROM route_stops;
CREATE TEMP TABLE voyage_id_map AS SELECT id AS old_id, new_id FROM voyages;
CREATE TEMP TABLE trip_id_map AS SELECT id AS old_id, new_id FROM trips;
CREATE TEMP TABLE order_id_map AS SELECT id AS old_id, new_id FROM ticket_orders;
CREATE TEMP TABLE ticket_id_map AS SELECT id AS old_id, new_id FROM tickets;

-- 4. Update foreign key references using mapping tables
UPDATE districts d SET new_id = (SELECT new_id FROM district_id_map WHERE old_id = d.id);
UPDATE agencies a SET new_district_id = (SELECT new_id FROM district_id_map WHERE old_id = a.district_id);
UPDATE users u SET new_agency_id = (SELECT new_id FROM agency_id_map WHERE old_id = u.agency_id) WHERE u.agency_id IS NOT NULL;
UPDATE routes r SET new_departure_district_id = (SELECT new_id FROM district_id_map WHERE old_id = r.departure_district_id);
UPDATE routes r SET new_arrival_district_id = (SELECT new_id FROM district_id_map WHERE old_id = r.arrival_district_id);
UPDATE route_stops rs SET new_route_id = (SELECT new_id FROM route_id_map WHERE old_id = rs.route_id);
UPDATE route_stops rs SET new_district_id = (SELECT new_id FROM district_id_map WHERE old_id = rs.district_id);
UPDATE voyages v SET new_route_id = (SELECT new_id FROM route_id_map WHERE old_id = v.route_id);
UPDATE trips t SET new_voyage_id = (SELECT new_id FROM voyage_id_map WHERE old_id = t.voyage_id);
UPDATE trips t SET new_bus_id = (SELECT new_id FROM bus_id_map WHERE old_id = t.bus_id) WHERE t.bus_id IS NOT NULL;
UPDATE trips t SET new_driver_user_id = (SELECT new_id FROM user_id_map WHERE old_id = t.driver_user_id) WHERE t.driver_user_id IS NOT NULL;
UPDATE trips t SET new_host_user_id = (SELECT new_id FROM user_id_map WHERE old_id = t.host_user_id) WHERE t.host_user_id IS NOT NULL;
UPDATE ticket_orders o SET new_buyer_id = (SELECT new_id FROM user_id_map WHERE old_id = o.buyer_id);
UPDATE tickets t SET new_trip_id = (SELECT new_id FROM trip_id_map WHERE old_id = t.trip_id);
UPDATE tickets t SET new_order_id = (SELECT new_id FROM order_id_map WHERE old_id = t.order_id);

-- 5. Drop old ID columns and rename new ones
-- Cities
ALTER TABLE cities DROP COLUMN id CASCADE;
ALTER TABLE cities RENAME COLUMN new_id TO id;
ALTER TABLE cities ADD PRIMARY KEY (id);

-- Districts
ALTER TABLE districts DROP COLUMN id CASCADE;
ALTER TABLE districts DROP COLUMN city_id CASCADE;
ALTER TABLE districts RENAME COLUMN new_id TO id;
ALTER TABLE districts ADD PRIMARY KEY (id);

-- Agencies
ALTER TABLE agencies DROP COLUMN id CASCADE;
ALTER TABLE agencies DROP COLUMN district_id CASCADE;
ALTER TABLE agencies RENAME COLUMN new_id TO id;
ALTER TABLE agencies RENAME COLUMN new_district_id TO district_id;
ALTER TABLE agencies ADD PRIMARY KEY (id);

-- Users
ALTER TABLE users DROP COLUMN id CASCADE;
ALTER TABLE users DROP COLUMN agency_id CASCADE;
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE users RENAME COLUMN new_agency_id TO agency_id;
ALTER TABLE users ADD PRIMARY KEY (id);

-- Buses
ALTER TABLE buses DROP COLUMN id CASCADE;
ALTER TABLE buses RENAME COLUMN new_id TO id;
ALTER TABLE buses ADD PRIMARY KEY (id);

-- Routes
ALTER TABLE routes DROP COLUMN id CASCADE;
ALTER TABLE routes DROP COLUMN departure_district_id CASCADE;
ALTER TABLE routes DROP COLUMN arrival_district_id CASCADE;
ALTER TABLE routes RENAME COLUMN new_id TO id;
ALTER TABLE routes RENAME COLUMN new_departure_district_id TO departure_district_id;
ALTER TABLE routes RENAME COLUMN new_arrival_district_id TO arrival_district_id;
ALTER TABLE routes ADD PRIMARY KEY (id);

-- Route Stops
ALTER TABLE route_stops DROP COLUMN id CASCADE;
ALTER TABLE route_stops DROP COLUMN route_id CASCADE;
ALTER TABLE route_stops DROP COLUMN district_id CASCADE;
ALTER TABLE route_stops RENAME COLUMN new_id TO id;
ALTER TABLE route_stops RENAME COLUMN new_route_id TO route_id;
ALTER TABLE route_stops RENAME COLUMN new_district_id TO district_id;
ALTER TABLE route_stops ADD PRIMARY KEY (id);

-- Voyages
ALTER TABLE voyages DROP COLUMN id CASCADE;
ALTER TABLE voyages DROP COLUMN route_id CASCADE;
ALTER TABLE voyages RENAME COLUMN new_id TO id;
ALTER TABLE voyages RENAME COLUMN new_route_id TO route_id;
ALTER TABLE voyages ADD PRIMARY KEY (id);

-- Trips
ALTER TABLE trips DROP COLUMN id CASCADE;
ALTER TABLE trips DROP COLUMN voyage_id CASCADE;
ALTER TABLE trips DROP COLUMN bus_id CASCADE;
ALTER TABLE trips DROP COLUMN driver_user_id CASCADE;
ALTER TABLE trips DROP COLUMN host_user_id CASCADE;
ALTER TABLE trips RENAME COLUMN new_id TO id;
ALTER TABLE trips RENAME COLUMN new_voyage_id TO voyage_id;
ALTER TABLE trips RENAME COLUMN new_bus_id TO bus_id;
ALTER TABLE trips RENAME COLUMN new_driver_user_id TO driver_user_id;
ALTER TABLE trips RENAME COLUMN new_host_user_id TO host_user_id;
ALTER TABLE trips ADD PRIMARY KEY (id);

-- Ticket Orders
ALTER TABLE ticket_orders DROP COLUMN id CASCADE;
ALTER TABLE ticket_orders DROP COLUMN buyer_id CASCADE;
ALTER TABLE ticket_orders RENAME COLUMN new_id TO id;
ALTER TABLE ticket_orders RENAME COLUMN new_buyer_id TO buyer_id;
ALTER TABLE ticket_orders ADD PRIMARY KEY (id);

-- Tickets
ALTER TABLE tickets DROP COLUMN id CASCADE;
ALTER TABLE tickets DROP COLUMN trip_id CASCADE;
ALTER TABLE tickets DROP COLUMN order_id CASCADE;
ALTER TABLE tickets RENAME COLUMN new_id TO id;
ALTER TABLE tickets RENAME COLUMN new_trip_id TO trip_id;
ALTER TABLE tickets RENAME COLUMN new_order_id TO order_id;
ALTER TABLE tickets ADD PRIMARY KEY (id);

-- Expenses
ALTER TABLE expenses DROP COLUMN id CASCADE;
ALTER TABLE expenses RENAME COLUMN new_id TO id;
ALTER TABLE expenses ADD PRIMARY KEY (id);

-- 6. Recreate foreign key constraints
ALTER TABLE districts ADD CONSTRAINT fk_districts_city FOREIGN KEY (city_id) REFERENCES cities(id);
ALTER TABLE agencies ADD CONSTRAINT fk_agencies_district FOREIGN KEY (district_id) REFERENCES districts(id);
ALTER TABLE users ADD CONSTRAINT fk_users_agency FOREIGN KEY (agency_id) REFERENCES agencies(id);
ALTER TABLE routes ADD CONSTRAINT fk_routes_departure FOREIGN KEY (departure_district_id) REFERENCES districts(id);
ALTER TABLE routes ADD CONSTRAINT fk_routes_arrival FOREIGN KEY (arrival_district_id) REFERENCES districts(id);
ALTER TABLE route_stops ADD CONSTRAINT fk_stops_route FOREIGN KEY (route_id) REFERENCES routes(id);
ALTER TABLE route_stops ADD CONSTRAINT fk_stops_district FOREIGN KEY (district_id) REFERENCES districts(id);
ALTER TABLE voyages ADD CONSTRAINT fk_voyages_route FOREIGN KEY (route_id) REFERENCES routes(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_voyage FOREIGN KEY (voyage_id) REFERENCES voyages(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_bus FOREIGN KEY (bus_id) REFERENCES buses(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_driver FOREIGN KEY (driver_user_id) REFERENCES users(id);
ALTER TABLE trips ADD CONSTRAINT fk_trips_host FOREIGN KEY (host_user_id) REFERENCES users(id);
ALTER TABLE ticket_orders ADD CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id);
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_trip FOREIGN KEY (trip_id) REFERENCES trips(id);
ALTER TABLE tickets ADD CONSTRAINT fk_tickets_order FOREIGN KEY (order_id) REFERENCES ticket_orders(id);

