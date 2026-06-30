Render :    testmutuelle4@gmail.com Mutuelle4test@  681050702
Gmail : mutuelle mobile   esnpy   01-01-2000 mutuelleenspy@gmail.com:Mutuelle4test@

spring.mail.username=alongamofranky@gmail.com
spring.mail.password=qhjc neen rwpc kzwq

commande pour lancer en local avec Mysql :
mvn spring-boot:run "-Dspring.profiles.active=dev"



#gandal
sudo -u postgres psql <<EOF
-- Supprimer les anciens
DROP DATABASE mutuelle_mobile;
DROP USER mutuelle_user;

-- Créer les nouveaux
CREATE USER mutuelle_user1 WITH PASSWORD 'root';
CREATE DATABASE mutuelle_mobile1 OWNER mutuelle_user1;
GRANT ALL PRIVILEGES ON DATABASE mutuelle_mobile1 TO mutuelle_user1;
EOF
 


# 4. Autoriser les connexions réseau
PG_VERSION=$(ls /etc/postgresql/)
PG_HBA="/etc/postgresql/$PG_VERSION/main/pg_hba.conf"
PG_CONF="/etc/postgresql/$PG_VERSION/main/postgresql.conf"

# Ecouter sur toutes les interfaces
sed -i "s/#listen_addresses = 'localhost'/listen_addresses = '*'/" $PG_CONF

# Autoriser le réseau local
echo "host    mutuelle_mobile1    mutuelle_user1    10.50.30.0/24    md5" >> $PG_HBA

# 5. Redémarrer PostgreSQL
systemctl restart postgresql

# 6. Vérification
systemctl status postgresql --no-pager
echo "---"
sudo -u postgres psql -c "\l" | grep mutuelle_mobile1


