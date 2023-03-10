Pre-installation requirements:

The following software is required to run this script:

1. Docker:
    • sudo apt-get update 
    • sudo apt-get install -y docker.io

2. Node.js:
    • sudo apt install -y nodejs
      
3. dhis2/cli:
    • sudo npm install -g @dhis2/cli

After download the script you need to chmod +x dhis-script.sh

The script takes a command-line argument to determine which function to call. The available options are:
    • reload: This argument spins up the DHIS2 instance and loads the dump.
    • up: This argument start the DHIS2 instance.
    • dump-db: This argument dumps the DHIS2 database to a compressed file.
    • down: This argument brings the DHIS2 instance down.

      Example usage ./dhis-script.sh reload
      
If an invalid argument is provided, the script displays the usage instructions and exits.

Downloading a Backup File from Atlassian.net

1. Log in to: 
    • https://openlmis.atlassian.net/wiki/spaces/SELV/pages/557121570/Credentials+restricted
 
2. Backup file should be at the end of the page

3.Click on the "Download" button to download the backup file to your local machine and save it in /tmp directory. If you can't indicate where to save the file when downloading it you need to manually move the backup file to the tmp directory. Open terminal, go to place where you save the backup file using cd command and then use this command: mv backup.sql.gz /tmp/

If you notice any error you need to check if u have Node.js version 14 or newer. You can check this using the node -v command. To download the correct version of Node.js run this command: sudo apt install -y nodejs 
