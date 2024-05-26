from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///vehicles.db'
db = SQLAlchemy(app)

class Location(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    vehicle_id = db.Column(db.String(50), nullable=False)
    latitude = db.Column(db.Float, nullable=False)
    longitude = db.Column(db.Float, nullable=False)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

@app.route('/api/locations', methods=['POST'])
def add_location():
    data = request.json
    new_location = Location(
        vehicle_id=data['vehicle_id'],
        latitude=data['latitude'],
        longitude=data['longitude'],
        timestamp=datetime.utcfromtimestamp(int(data['timestamp']) / 1000)
    )
    db.session.add(new_location)
    db.session.commit()
    return jsonify({"message": "Location added"}), 200

@app.route('/api/locations', methods=['GET'])
def get_locations():
    locations = Location.query.all()
    return jsonify([{
        "vehicle_id": loc.vehicle_id,
        "latitude": loc.latitude,
        "longitude": loc.longitude,
        "timestamp": loc.timestamp
    } for loc in locations]), 200

if __name__ == '__main__':
    db.create_all()
    app.run(debug=True)
