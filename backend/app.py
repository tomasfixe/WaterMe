import os
import psycopg2
import bcrypt
from flask import Flask, request, jsonify
from flask_cors import CORS
from datetime import datetime

app = Flask(__name__)
CORS(app) # Permite que a App Android aceda à API

# --- LIGAÇÃO À BASE DE DADOS ---
def get_db_connection():
    try:
        return psycopg2.connect(os.environ.get('DATABASE_URL'))
    except Exception as e:
        print(f"Erro DB: {e}")
        return None

# --- CRIAÇÃO DAS TABELAS ---
def create_tables():
    conn = get_db_connection()
    if conn:
        cur = conn.cursor()
        # Tabela Users
        cur.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            );
        """)
        # Tabela Plants
        cur.execute("""
            CREATE TABLE IF NOT EXISTS plants (
                id SERIAL PRIMARY KEY,
                user_id INTEGER REFERENCES users(id),
                name TEXT NOT NULL,
                description TEXT,
                photo_url TEXT,
                last_watering TEXT,
                next_watering TEXT,
                light_level REAL
            );
        """)
        conn.commit()
        cur.close()
        conn.close()

create_tables()

# --- ROTAS DA API ---

@app.route('/')
def home():
    return "Water Me API"

# --- ROTA TEMPORÁRIA PARA ATUALIZAR A BD ---

@app.route('/update_db_light', methods=['GET'])
def update_db_light():
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        # Cria a coluna se ela não existir
        cur.execute("ALTER TABLE plants ADD COLUMN IF NOT EXISTS light_level REAL;")
        conn.commit()
        cur.close()
        conn.close()
        return "Sucesso! Coluna light_level criada.", 200
    except Exception as e:
        return f"Erro: {e}", 500


# 1. REGISTAR USER
@app.route('/auth/register', methods=['POST'])
def register():
    data = request.get_json()
    hashed = bcrypt.hashpw(data['password'].encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

    conn = get_db_connection()
    cur = conn.cursor()
    try:
        cur.execute("INSERT INTO users (name, email, password) VALUES (%s, %s, %s) RETURNING id;",
                    (data['name'], data['email'], hashed))
        new_id = cur.fetchone()[0]
        conn.commit()
        return jsonify({"id": new_id, "message": "Conta criada"}), 201
    except:
        return jsonify({"error": "Email já existe"}), 400
    finally:
        cur.close(); conn.close()

# 2. LOGIN
@app.route('/auth/login', methods=['POST'])
def login():
    data = request.get_json()
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("SELECT id, name, password FROM users WHERE email = %s;", (data['email'],))
    user = cur.fetchone()
    cur.close(); conn.close()

    if user and bcrypt.checkpw(data['password'].encode('utf-8'), user[2].encode('utf-8')):
        return jsonify({"message": "Sucesso", "user_id": user[0], "name": user[1]}), 200
    return jsonify({"error": "Dados errados"}), 401

# 3. GUARDAR NOVA PLANTA
@app.route('/plants', methods=['POST'])
def add_plant():
    data = request.get_json()
    conn = get_db_connection()
    cur = conn.cursor()
    try:
        cur.execute("""
            INSERT INTO plants (user_id, name, description, photo_url, last_watering, next_watering, light_level)
            VALUES (%s, %s, %s, %s, %s, %s, %s) RETURNING id;
        """, (
            data['user_id'],
            data['name'],
            data.get('description', ''),
            data.get('photo_url', ''),
            data.get('last_watering', datetime.now().isoformat()),
            data['next_watering'],
            data.get('light_level', 0.0) # <--- NOVO (Padrão é 0.0)
        ))
        new_id = cur.fetchone()[0]
        conn.commit()
        return jsonify({"id": new_id, "message": "Guardado!"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        cur.close(); conn.close()

# 4. LISTAR PLANTAS
@app.route('/plants/<int:user_id>', methods=['GET'])
def get_plants(user_id):
    conn = get_db_connection()
    cur = conn.cursor()
    # SELECT * busca automaticamente a nova coluna light_level
    cur.execute("SELECT * FROM plants WHERE user_id = %s ORDER BY id DESC;", (user_id,))
    cols = [desc[0] for desc in cur.description]
    results = [dict(zip(cols, row)) for row in cur.fetchall()]
    cur.close(); conn.close()
    return jsonify(results), 200

# 5. APAGAR PLANTA
@app.route('/plants/<int:plant_id>', methods=['DELETE'])
def delete_plant(plant_id):
    conn = get_db_connection()
    cur = conn.cursor()
    cur.execute("DELETE FROM plants WHERE id = %s;", (plant_id,))
    conn.commit()
    cur.close(); conn.close()
    return jsonify({"message": "Apagada"}), 200

# 6. EDITAR PLANTA
@app.route('/plants/<int:plant_id>', methods=['PUT'])
def update_plant(plant_id):
    data = request.get_json()
    conn = get_db_connection()
    cur = conn.cursor()
    try:
        # Atualiza apenas os campos desta planta específica
        cur.execute("""
            UPDATE plants
            SET name = %s, description = %s, photo_url = %s, next_watering = %s, last_watering = %s, light_level = %s
            WHERE id = %s;
        """, (
            data['name'],
            data.get('description', ''),
            data.get('photo_url', ''),
            data['next_watering'],
            data.get('last_watering', datetime.now().isoformat()),
            data.get('light_level', 0.0), # <--- NOVO
            plant_id
        ))
        conn.commit()
        return jsonify({"message": "Planta atualizada!"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500
    finally:
        cur.close(); conn.close()

if __name__ == '__main__':
    app.run(debug=True)