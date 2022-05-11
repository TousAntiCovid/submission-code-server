#!/bin/bash
set -e

echo "==> Create upload directory"
mkdir -p /home/user/upload || true
chmod -R 007 /home/user/upload || true

echo "==> Add authorized_keys"
chmod 600 /home/user/.ssh/authorized_keys || true
chown -R user: /home/user/.ssh/authorized_keys || true