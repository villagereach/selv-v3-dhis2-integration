#!/bin/bash

# Set the DHIS2 instance name and git repo URL
INSTANCE_NAME="master"
image_version="dhis2/core-dev:2.37"
container="d2-cluster-master_core_1"

# Define the function to spin up the DHIS2 instance and load the dump
function spin_up_instance() {
    echo "Spinning up DHIS2 instance..."
    d2 cluster up $INSTANCE_NAME -i=$image_version

    # Stop the instance to load the dump
    echo "Stopping DHIS2 instance to load dump..."
    d2 cluster down $INSTANCE_NAME --clean

    # Load the dump
    d2 cluster db restore $INSTANCE_NAME ~/tmp/backup.sql.gz

    #start the instance
    d2 cluster up $INSTANCE_NAME -i=$image_version
    rm -rf /tmp/backup.sql.gz
}   

# Define the function to start the DHIS2 instance
function up_instance() {
    echo "Spinning up DHIS2 instance..."
    d2 cluster up $INSTANCE_NAME -i=$image_version
}

# Define the function to dump the DHIS2 database
function dump_database() {
    filename="backup_$(date +%Y-%m-%d_%H-%M-%S).sql.gz"
    d2 cluster db backup $INSTANCE_NAME $filename
}

# Define the function to bring the DHIS2 instance down
function bring_down_instance() {
    echo "Bringing DHIS2 instance down..."
    d2 cluster down $INSTANCE_NAME
}

# Check the command-line argument and call the appropriate function
case $1 in
    "reload")
        spin_up_instance
        ;;
    "up")
        up_instance
        ;;    
    "dump-db")
        dump_database
        ;;
    "down")
        bring_down_instance
        ;;
    *)
        echo "Usage: $0 reload|up|dump-db|down"
        exit 1
        ;;
esac

