import os, io, subprocess, glob

cp_entries = ['target/classes']
cp_file = io.open('cp.txt', encoding='utf-8').read().strip()
for jar in cp_file.split(';'):
    jar = jar.replace('\\', '/')
    if os.path.exists(jar):
        cp_entries.append(jar)

cp = ';'.join(cp_entries)

sources = glob.glob('src/main/java/com/resumeforge/**/*.java', recursive=True)
print(f'Compiling {len(sources)} files with --release 17 -parameters ...')
cmd = ['javac', '--release', '17', '-parameters', '-cp', cp, '-d', 'target/classes'] + sources
result = subprocess.run(cmd, capture_output=True, text=True)
if result.stderr and 'error' in result.stderr.lower():
    print('ERRORS:', result.stderr[:2000])
elif result.stderr:
    print('Notes:', result.stderr[:300])
print(f'Exit code: {result.returncode}')
