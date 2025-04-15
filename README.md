# AFIS Server Deduplication Tool

## Overview
The AFIS Server Deduplication Tool is a command-line application for performing biometric template deduplication. It supports:
- Processing templates from files or database
- Matching biometric templates (fingerprints)
- Outputting match results to CSV files

## Requirements
- Java 8 or higher
- Database with biometric templates (if using database mode)
- Sufficient memory for template processing

## Installation
1. Extract the application package to your desired location
2. Configure the `application.properties` file
3. Run the application using the jar

## Configuration
Create or modify the `application.properties` file in the same directory as the application:

```properties
# Server connection settings
server.address=localhost
server.client.port=25452
server.admin.port=24932

# Template source settings
template.source.db=false
template.directory=D:/NeuroTechnology/templates

# Database Configuration (when template.source.db=true)
database.dsn=jdbc:sqlserver://167.86.68.15:1433;databaseName=SNSOP_AFIS_TEST;encrypt=false
database.username=snsop
database.password=Win@2019!@#$
database.table=Subjects
database.template.column=Template
database.id.column=SubjectId

# Results output
dir.result=D:/NeuroTechnology/results.csv
```

### Configuration Options

#### Server Connection
- `server.address`: Address of the AFIS server
- `server.client.port`: Port for client connections
- `server.admin.port`: Port for administrative connections

#### Database Configuration
- `database.dsn`: JDBC connection string for the database
- `database.username`: Database username
- `database.password`: Database password
- `database.table`: Table containing the templates
- `database.template.column`: Column name for biometric templates
- `database.id.column`: Column name for subject/person identifiers

#### Results
- `dir.result`: File path where the deduplication results CSV will be saved

## Usage

### Running the Application
To run the application, use:

```bash
java -jar AFISServer.jar
```