import uuid
import random

from faker import Faker
import psycopg2
from psycopg2.extras import execute_batch

fake = Faker()

# ==========================================
# CONFIG
# ==========================================

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "production",
    "user": "username",
    "password": "password"
}

USERS_COUNT = 1000
GAMES_COUNT = 1000
MODS_COUNT = 1000
VERSIONS_COUNT = 1000
FILES_COUNT = 1000
COMMENTS_COUNT = 1000
COLLECTIONS_COUNT = 1000

VERSION_STATUSES = ["PENDING", "APPROVED", "REJECTED"]

# ==========================================
# CONNECTION
# ==========================================

conn = psycopg2.connect(**DB_CONFIG)
cur = conn.cursor()

# ==========================================
# USERS
# ==========================================

print("Generating users...")

users = []
usernames = []

for i in range(USERS_COUNT):
    username = f"user_{i}"

    usernames.append(username)

    users.append(
        (
            username,
            f"Name {i}",
            f"user{i}@mail.com",
            fake.password(length=12),
            random.choice([True, False])
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.users
    (username, name, email, password, is_admin)
    VALUES (%s, %s, %s, %s, %s)
    """,
    users,
    page_size=1000
)

# ==========================================
# GAMES
# ==========================================

print("Generating games...")

games = []
game_ids = []

for i in range(GAMES_COUNT):
    game_id = f"game_{i}"

    game_ids.append(game_id)

    games.append(
        (
            game_id,
            f"Game {i}",
            fake.text(max_nb_chars=200),
            f"/images/game_{i}.jpg"
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.games
    (id, name, description, image_path)
    VALUES (%s, %s, %s, %s)
    """,
    games,
    page_size=1000
)

# ==========================================
# CATEGORIES
# ==========================================

print("Generating categories...")

categories = [
    ("Graphics", "Visual improvements and textures"),
    ("Gameplay", "Gameplay changes"),
    ("Weapons", "Weapons and combat"),
    ("Armor", "Armor and equipment"),
    ("Characters", "New characters and NPCs"),
    ("Quests", "Additional quests"),
    ("Maps", "New maps and locations"),
    ("Vehicles", "Vehicles and transport"),
    ("UI", "User interface improvements"),
    ("Audio", "Music and sound effects"),
    ("Animations", "Animation changes"),
    ("Performance", "Optimization and performance"),
    ("Survival", "Survival mechanics"),
    ("Magic", "Magic systems and spells"),
    ("Cheats", "Cheats and trainers"),
    ("Multiplayer", "Multiplayer features"),
    ("Lore", "Story and lore expansion"),
    ("Buildings", "Construction and buildings"),
    ("Crafting", "Crafting systems"),
    ("Utility", "Quality-of-life improvements")
]

category_names = [name for name, _ in categories]

execute_batch(
    cur,
    """
    INSERT INTO modweave.categories
    (name, description)
    VALUES (%s, %s)
    """,
    categories,
    page_size=1000
)

# ==========================================
# MODS
# ==========================================

print("Generating mods...")

mods = []
mod_ids = []

for i in range(MODS_COUNT):
    mod_id = f"mod_{i}"

    mod_ids.append(mod_id)

    mods.append(
        (
            mod_id,
            f"Mod {i}",
            fake.text(max_nb_chars=300),
            f"/mods/mod_{i}.png",
            random.choice(game_ids),
            random.choice(usernames)
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.mods
    (id, name, description, image_path, game_id, publisher_id)
    VALUES (%s, %s, %s, %s, %s, %s)
    """,
    mods,
    page_size=1000
)

# ==========================================
# MOD VERSIONS
# ==========================================

print("Generating mod_versions...")

versions = []
version_ids = []

for i in range(VERSIONS_COUNT):
    version_id = str(uuid.uuid4())
    version_ids.append(version_id)

    mod_id = random.choice(mod_ids)

    versions.append(
        (
            version_id,
            f"version_{i}",  # гарантированно уникально
            fake.text(max_nb_chars=150),
            random.choice(VERSION_STATUSES),
            mod_id
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.mod_versions
    (id, name, changes, status, mod_id)
    VALUES (%s, %s, %s, %s, %s)
    """,
    versions,
    page_size=1000
)

# ==========================================
# MOD FILES
# ==========================================

print("Generating mod_files...")

files = []

for i in range(FILES_COUNT):
    files.append(
        (
            str(uuid.uuid4()),
            f"file_{i}.zip",
            f"/files/file_{i}.zip",
            random.randint(0, 100000),
            random.choice(version_ids)
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.mod_files
    (id, filename, file_path, downloads, mod_version_id)
    VALUES (%s, %s, %s, %s, %s)
    """,
    files,
    page_size=1000
)

# ==========================================
# MODS CATEGORIES
# ==========================================

print("Generating mods_categories...")

mods_categories = set()

while len(mods_categories) < 3000:
    mods_categories.add(
        (
            random.choice(mod_ids),
            random.choice(category_names)
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.mods_categories
    (mod_id, category_name)
    VALUES (%s, %s)
    """,
    list(mods_categories),
    page_size=1000
)

# ==========================================
# COMMENTS
# ==========================================

print("Generating comments...")

comments = []

for _ in range(COMMENTS_COUNT):
    comments.append(
        (
            str(uuid.uuid4()),
            fake.paragraph(nb_sentences=5),
            random.choice(usernames),
            random.choice(mod_ids)
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.comments
    (id, content, user_id, mod_id)
    VALUES (%s, %s, %s, %s)
    """,
    comments,
    page_size=1000
)

# ==========================================
# COLLECTIONS
# ==========================================

print("Generating collections...")

collections = []
collection_ids = []

for i in range(COLLECTIONS_COUNT):
    collection_id = str(uuid.uuid4())

    collection_ids.append(collection_id)

    collections.append(
        (
            collection_id,
            f"Collection_{i}",
            fake.text(max_nb_chars=100),
            random.choice(usernames)
        )
    )

execute_batch(
    cur,
    """
    INSERT INTO modweave.collections
    (id, name, description, owner)
    VALUES (%s, %s, %s, %s)
    """,
    collections,
    page_size=1000
)

# ==========================================
# COLLECTIONS_MODS
# ==========================================

print("Generating collections_mods...")

collection_mods = []

for collection_id in collection_ids:

    mods_for_collection = random.sample(
        mod_ids,
        k=min(5, len(mod_ids))
    )

    for order_index, mod_id in enumerate(mods_for_collection, start=1):
        collection_mods.append(
            (
                collection_id,
                order_index,
                mod_id
            )
        )

execute_batch(
    cur,
    """
    INSERT INTO modweave.collections_mods
    (collection_id, order_index, mod_id)
    VALUES (%s, %s, %s)
    """,
    collection_mods,
    page_size=1000
)

# ==========================================
# COMMIT
# ==========================================

conn.commit()

cur.close()
conn.close()

print("Done.")
print(f"Users: {USERS_COUNT}")
print(f"Games: {GAMES_COUNT}")
print(f"Mods: {MODS_COUNT}")
print(f"Versions: {VERSIONS_COUNT}")
print(f"Files: {FILES_COUNT}")
print(f"Categories: {len(categories)}")
print(f"Comments: {COMMENTS_COUNT}")
print(f"Collections: {COLLECTIONS_COUNT}")
print(f"Collection Mods: {len(collection_mods)}")
print(f"Mods Categories: {len(mods_categories)}")
