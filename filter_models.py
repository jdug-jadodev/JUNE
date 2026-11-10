import json
data = json.load(open('models.json'))
models = data.get('data', [])
for m in models:
    if m.get('providerID') in ('nvidia', 'orcarouter'):
        print(m.get('id', ''), '|', m.get('providerID', ''), '|', m.get('name', ''))