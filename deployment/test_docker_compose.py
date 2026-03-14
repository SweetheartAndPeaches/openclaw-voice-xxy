#!/usr/bin/env python3
"""
Docker Compose Integration Test Script
====================================

This script validates the end-to-end functionality of the podcast website
by testing the complete workflow including backend, frontend, and Coze Voice Gen API integration.

Author: OpenClaw-Coder
Task ID: podcast-012
"""

import os
import sys
import time
import subprocess
import requests
from typing import Dict, List, Optional, Tuple
from pathlib import Path


class DockerComposeTest:
    """Docker Compose integration test orchestrator."""
    
    def __init__(self, project_root: str = "/workspace/projects/podcast-website"):
        """
        Initialize the test environment.
        
        Args:
            project_root: Root directory of the podcast website project
        """
        self.project_root = Path(project_root)
        self.deployment_dir = self.project_root / "deployment"
        self.backend_dir = self.project_root / "backend"
        self.frontend_dir = self.project_root / "frontend"
        self.test_results: Dict[str, bool] = {}
        
    def check_prerequisites(self) -> bool:
        """
        Check if all prerequisites are met for Docker Compose testing.
        
        Returns:
            bool: True if all prerequisites are met, False otherwise
        """
        print("🔍 Checking prerequisites...")
        
        # Check if Docker is installed and running
        try:
            result = subprocess.run(
                ["docker", "--version"], 
                capture_output=True, 
                text=True, 
                timeout=10
            )
            if result.returncode != 0:
                print("❌ Docker is not installed or not accessible")
                return False
            print(f"✅ Docker version: {result.stdout.strip()}")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            print("❌ Docker is not installed or not accessible")
            return False
            
        # Check if docker-compose is available
        try:
            result = subprocess.run(
                ["docker-compose", "--version"], 
                capture_output=True, 
                text=True, 
                timeout=10
            )
            if result.returncode != 0:
                print("❌ docker-compose is not installed")
                return False
            print(f"✅ docker-compose version: {result.stdout.strip()}")
        except (subprocess.TimeoutExpired, FileNotFoundError):
            print("❌ docker-compose is not installed")
            return False
            
        # Check if required directories exist
        required_dirs = [self.deployment_dir, self.backend_dir, self.frontend_dir]
        for dir_path in required_dirs:
            if not dir_path.exists():
                print(f"❌ Required directory missing: {dir_path}")
                return False
        print("✅ All required directories exist")
        
        # Check if docker-compose.yml exists
        compose_file = self.deployment_dir / "docker-compose.yml"
        if not compose_file.exists():
            print(f"❌ docker-compose.yml not found: {compose_file}")
            return False
        print("✅ docker-compose.yml exists")
        
        return True
    
    def start_services(self) -> bool:
        """
        Start backend and frontend services using Docker Compose.
        
        Returns:
            bool: True if services started successfully, False otherwise
        """
        print("🚀 Starting services with Docker Compose...")
        
        try:
            # Change to deployment directory
            os.chdir(self.deployment_dir)
            
            # Build and start services in detached mode
            result = subprocess.run(
                ["docker-compose", "up", "--build", "-d"],
                capture_output=True,
                text=True,
                timeout=120
            )
            
            if result.returncode != 0:
                print(f"❌ Failed to start services:\n{result.stderr}")
                return False
                
            print("✅ Services started successfully")
            print(f"stdout: {result.stdout}")
            
            # Wait for services to be ready
            time.sleep(15)
            return True
            
        except subprocess.TimeoutExpired:
            print("❌ Timeout while starting services")
            return False
        except Exception as e:
            print(f"❌ Error starting services: {e}")
            return False
    
    def test_backend_health(self) -> bool:
        """
        Test if the backend service is healthy and responding.
        
        Returns:
            bool: True if backend is healthy, False otherwise
        """
        print("🏥 Testing backend health...")
        
        try:
            response = requests.get(
                "http://localhost:8080/actuator/health",
                timeout=10
            )
            if response.status_code == 200:
                print("✅ Backend health check passed")
                return True
            else:
                print(f"❌ Backend health check failed: {response.status_code}")
                return False
        except requests.exceptions.RequestException as e:
            print(f"❌ Backend health check error: {e}")
            return False
    
    def test_voice_api_integration(self) -> bool:
        """
        Test Coze Voice Gen API integration through the backend.
        
        Returns:
            bool: True if API integration works, False otherwise
        """
        print("🎤 Testing Coze Voice Gen API integration...")
        
        try:
            # Test the voice task endpoint
            test_payload = {
                "text": "Hello, this is a test message for voice generation.",
                "voice_id": "default",
                "user_id": "test_user"
            }
            
            response = requests.post(
                "http://localhost:8080/api/voice/tasks",
                json=test_payload,
                timeout=30
            )
            
            if response.status_code in [200, 201]:
                print("✅ Coze Voice Gen API integration test passed")
                return True
            else:
                print(f"❌ Coze Voice Gen API integration failed: {response.status_code}")
                print(f"Response: {response.text}")
                return False
                
        except requests.exceptions.RequestException as e:
            print(f"❌ Coze Voice Gen API integration error: {e}")
            return False
    
    def test_frontend_accessibility(self) -> bool:
        """
        Test if the frontend is accessible.
        
        Returns:
            bool: True if frontend is accessible, False otherwise
        """
        print("🌐 Testing frontend accessibility...")
        
        try:
            response = requests.get(
                "http://localhost:3000",
                timeout=10
            )
            if response.status_code == 200:
                print("✅ Frontend accessibility test passed")
                return True
            else:
                print(f"❌ Frontend accessibility test failed: {response.status_code}")
                return False
        except requests.exceptions.RequestException as e:
            print(f"❌ Frontend accessibility error: {e}")
            return False
    
    def stop_services(self) -> bool:
        """
        Stop and clean up Docker Compose services.
        
        Returns:
            bool: True if services stopped successfully, False otherwise
        """
        print("⏹️ Stopping services...")
        
        try:
            os.chdir(self.deployment_dir)
            result = subprocess.run(
                ["docker-compose", "down"],
                capture_output=True,
                text=True,
                timeout=60
            )
            
            if result.returncode != 0:
                print(f"❌ Failed to stop services:\n{result.stderr}")
                return False
                
            print("✅ Services stopped successfully")
            return True
            
        except subprocess.TimeoutExpired:
            print("❌ Timeout while stopping services")
            return False
        except Exception as e:
            print(f"❌ Error stopping services: {e}")
            return False
    
    def run_full_test_suite(self) -> bool:
        """
        Run the complete end-to-end test suite.
        
        Returns:
            bool: True if all tests pass, False otherwise
        """
        print("=" * 60)
        print("🧪 STARTING DOCKER COMPOSE INTEGRATION TEST SUITE")
        print("=" * 60)
        
        # Step 1: Check prerequisites
        if not self.check_prerequisites():
            return False
        
        # Step 2: Start services
        if not self.start_services():
            self.stop_services()  # Clean up even if start failed
            return False
        
        try:
            # Step 3: Run individual tests
            tests = [
                ("Backend Health", self.test_backend_health),
                ("Voice API Integration", self.test_voice_api_integration),
                ("Frontend Accessibility", self.test_frontend_accessibility)
            ]
            
            all_passed = True
            for test_name, test_func in tests:
                print(f"\n--- Running {test_name} ---")
                result = test_func()
                self.test_results[test_name] = result
                if not result:
                    all_passed = False
            
            return all_passed
            
        finally:
            # Always clean up
            self.stop_services()
    
    def generate_test_report(self) -> str:
        """
        Generate a comprehensive test report.
        
        Returns:
            str: Formatted test report
        """
        report = []
        report.append("📊 DOCKER COMPOSE INTEGRATION TEST REPORT")
        report.append("=" * 50)
        
        total_tests = len(self.test_results)
        passed_tests = sum(1 for result in self.test_results.values() if result)
        failed_tests = total_tests - passed_tests
        
        report.append(f"Total Tests: {total_tests}")
        report.append(f"Passed: {passed_tests}")
        report.append(f"Failed: {failed_tests}")
        report.append(f"Success Rate: {passed_tests/total_tests*100:.1f}%" if total_tests > 0 else "Success Rate: N/A")
        report.append("")
        
        for test_name, result in self.test_results.items():
            status = "✅ PASS" if result else "❌ FAIL"
            report.append(f"{test_name}: {status}")
        
        return "\n".join(report)


def main():
    """Main entry point for the Docker Compose integration test."""
    test_runner = DockerComposeTest()
    
    try:
        success = test_runner.run_full_test_suite()
        report = test_runner.generate_test_report()
        print("\n" + report)
        
        if success:
            print("\n🎉 ALL TESTS PASSED! End-to-end functionality is working correctly.")
            return 0
        else:
            print("\n💥 SOME TESTS FAILED! Please check the report above.")
            return 1
            
    except KeyboardInterrupt:
        print("\n⚠️ Test interrupted by user")
        test_runner.stop_services()
        return 1
    except Exception as e:
        print(f"\n💥 Unexpected error during testing: {e}")
        test_runner.stop_services()
        return 1


if __name__ == "__main__":
    sys.exit(main())